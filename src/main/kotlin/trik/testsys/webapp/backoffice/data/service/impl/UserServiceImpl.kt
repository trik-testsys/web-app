package trik.testsys.webapp.backoffice.data.service.impl

import org.slf4j.LoggerFactory
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Predicate
import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.entity.impl.UserGroup
import trik.testsys.webapp.backoffice.data.repository.UserRepository
import trik.testsys.webapp.backoffice.data.service.UserService
import trik.testsys.webapp.backoffice.data.service.UserGroupService
import trik.testsys.webapp.backoffice.data.service.SuperUserService
import trik.testsys.webapp.backoffice.data.service.ViewerService
import trik.testsys.webapp.core.data.service.AbstractService
import trik.testsys.webapp.core.data.entity.AbstractEntity
import java.time.Instant
import kotlin.random.Random

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
class UserServiceImpl(
    private val accessTokenService: AccessTokenService,
    private val regTokenService: RegTokenService,
    private val userGroupService: UserGroupService
) :
    AbstractService<User, UserRepository>(),
    UserService, ViewerService, SuperUserService {

    override fun updateName(user: User, newName: String): User {
        user.name = newName
        return save(user)
    }

    override fun updateEmail(user: User, newEmail: String?): User {
        user.email = newEmail
        user.emailVerifiedAt = null
        user.requestedEmailDetach = false

        return save(user)
    }

    override fun updateLastLoginAt(user: User, lastLoginAt: Instant?): User {
        lastLoginAt?.let {
            user.lastLoginAt = it
        } ?: run { user.lastLoginAt = Instant.now() }

        return save(user)
    }

    override fun createAdmin(viewer: User, name: String?): User? {
        viewer.adminRegToken ?: run {
            logger.warn("Could not create user for VIEWER privileged user.")
            return null
        }

        val accessToken = accessTokenService.generate()
        val admin = User().also {
            it.accessToken = accessToken

            it.viewer = viewer
            it.privileges.add(User.Privilege.ADMIN)
            it.name = name ?: "New User ${Random.nextInt()}"
        }

        // Persist the new admin first so that other persistent entities can safely reference it
        val persistedAdmin = save(admin)
        // set inverse side after user is persisted to avoid transient reference during flush
        accessToken.user = persistedAdmin
        // now update viewer relationships
        viewer.managedAdmins.add(persistedAdmin)
        save(viewer)

        // Ensure admin is a member of all groups the viewer is a member of (including viewer-owned groups)
        // Use the owning side of the relation via service to persist join-table rows
        viewer.memberedGroups.forEach { group ->
            userGroupService.addMember(group, persistedAdmin)
        }

        return persistedAdmin
    }

    override fun createUser(superUser: User, name: String, privileges: Collection<User.Privilege>): Boolean {
        if (!superUser.privileges.contains(User.Privilege.SUPER_USER)) {
            logger.warn(
                "Could not create new user(name=${name}, privileges=${privileges}) for user(id=${superUser.id}), " +
                        "it has no SuperUser privileges."
            )
            return false
        }

        val accessToken = accessTokenService.generate()
        val newUser = User().also {
            it.superUser = superUser
            it.accessToken = accessToken
            it.name = name
        }

        // Persist the new user before assigning it to collections or creating tokens referencing it
        val persistedUser = save(newUser)
        // set inverse side after user is persisted to avoid transient reference during flush
        accessToken.user = persistedUser
        // assign privileges after the user is persistent (may create RegToken referencing user)
        addPrivileges(superUser, persistedUser, privileges)
        // link to creator after persistence
        superUser.createdUsers.add(persistedUser)
        save(superUser)

        return true
    }

    override fun createUserByGroupAdmin(
        groupAdmin: User,
        name: String,
        privileges: Collection<User.Privilege>
    ): User? {
        if (!groupAdmin.privileges.contains(User.Privilege.GROUP_ADMIN)) {
            logger.warn(
                "Could not create new user(name=$name, privileges=$privileges) for user(id=${groupAdmin.id}), it has no GROUP_ADMIN privileges."
            )
            return null
        }

        // Restrict privileges to allowed set for group admin
        val allowed = setOf(
            User.Privilege.DEVELOPER,
            User.Privilege.JUDGE,
            User.Privilege.VIEWER,
        )
        val requested = privileges.toSet().intersect(allowed)

        val trimmed = name.trim()
        if (trimmed.isEmpty()) return null

        val accessToken = accessTokenService.generate()
        val newUser = User().also {
            it.accessToken = accessToken
            it.name = trimmed
        }

        val persisted = save(newUser)
        accessToken.user = persisted

        // assign only allowed privileges
        requested.forEach { p ->
            // mimic addPrivilege logic for VIEWER special case
            persisted.privileges.add(p)
            if (p == User.Privilege.VIEWER && persisted.adminRegToken == null) {
                val regToken = regTokenService.generate()
                persisted.adminRegToken = regToken
                regToken.viewer = persisted
            }
        }
        save(persisted)

        return persisted
    }

    override fun addPrivilege(superUser: User, user: User, privilege: User.Privilege): Boolean {
        if (!superUser.privileges.contains(User.Privilege.SUPER_USER)) {
            logger.warn(
                "Could not add privilege(privilege=$privilege) to user(id=${user.id}) by user(id=${superUser.id}), " +
                        "it has no SuperUser privileges."
            )
            return false
        }

        if (user.privileges.contains(privilege)) {
            logger.warn("Could not add already present privilege(privilege=$privilege) to user(id=${user.id}).")
            return false
        }

        logger.debug("Adding privilege(privilege=$privilege) to user(id=${user.id}).")
        user.privileges.add(privilege)

        if (privilege == User.Privilege.VIEWER && user.adminRegToken == null) {
            logger.info("User(id=${user.id}) granted VIEWER. Generating adminRegToken.")

            val regToken = regTokenService.generate()
            user.adminRegToken = regToken
            regToken.viewer = user
        }

        save(user)
        return true
    }

    override fun removeUser(superUser: User, user: User): Boolean {
        if (!superUser.privileges.contains(User.Privilege.SUPER_USER)) {
            logger.warn(
                "Could not remove user(id=${user.id}) by user(id=${superUser.id}), it has no SuperUser privileges."
            )
            return false
        }

        if (!superUser.isAllUserSuperUser && user.superUser?.id != superUser.id) {
            logger.warn("Could not remove user(id=${user.id}) not created by current SuperUser(id=${superUser.id}).")
            return false
        }

        if (user.lastLoginAt != null) {
            logger.warn("Could not remove user(id=${user.id}) that already logged in.")
            return false
        }

        if (user.isRemoved) {
            logger.info("User(id=${user.id}) already marked as removed.")
            return true
        }

        user.isRemoved = true
        save(user)
        return true
    }

    override fun findAllSuperUser(isAllUserSuperUser: Boolean?) = repository.findAll { root, q, cb ->
        val predicates = mutableListOf<Predicate>()

        root.fetch<Any, Any>(User.ACCESS_TOKEN, JoinType.LEFT)
        q?.distinct(true)

        val privilegesPath = root.get<Set<User.Privilege>>(User.PRIVILEGES)
        val hasSuperUserPrivilege = cb.isMember(User.Privilege.SUPER_USER, privilegesPath)
        predicates.add(hasSuperUserPrivilege)

        isAllUserSuperUser?.let {
            val allUserFlagMatches = cb.equal(root.get<Boolean>(User.IS_ALL_USER_SUPER_USER), isAllUserSuperUser)
            predicates.add(allUserFlagMatches)
        }

        cb.and(*predicates.toTypedArray())
    }.toSet()

    override fun findAllGroupAdmin() = repository.findAll { root, q, cb ->
        root.fetch<Any, Any>(User.ACCESS_TOKEN, JoinType.LEFT)
        q?.distinct(true)
        val privilegesPath = root.get<Set<User.Privilege>>(User.PRIVILEGES)
        cb.isMember(User.Privilege.GROUP_ADMIN, privilegesPath)
    }.toSet()

    override fun findAllBySuperUser(superUser: User): Set<User> {
        return repository.findAllBySuperUser(superUser)
    }

    override fun save(entity: User): User {
        val isNewEntity = entity.isNew
        val persisted = super.save(entity)

        if (isNewEntity) {
            // Avoid creating the default group during arbitrary user creation to prevent FK/order issues.
            // If the default group already exists, just add the member; otherwise startup runner will create it.
            userGroupService.getDefaultGroup()?.let { defaultGroup ->
                userGroupService.addMember(defaultGroup, persisted)
            }
        }

        return persisted
    }

    override fun findCandidatesFor(userGroup: UserGroup): Set<User> {
        val ownerId = userGroup.owner?.id
        val memberIds = userGroup.activeMembers.mapNotNull { it.id }.toSet()

        return repository.findAll { root, q, cb ->
            val predicates = mutableListOf<Predicate>()

            q?.distinct(true)

            // Exclude removed users
            predicates.add(cb.isFalse(root.get<Boolean>(User.IS_REMOVED)))

            // Exclude group owner if present
            ownerId?.let { predicates.add(cb.notEqual(root.get<Long>(AbstractEntity.ID), it)) }

            // Exclude already membered users
            if (memberIds.isNotEmpty()) {
                val idPath = root.get<Long>(AbstractEntity.ID)
                predicates.add(idPath.`in`(memberIds).not())
            }

            cb.and(*predicates.toTypedArray())
        }.toSet()
    }

    override fun findByEmail(email: String): User? {
        val user = repository.findByEmailAndEmailVerifiedAtNotNull(email)

        return user
    }

    companion object {

        private val logger = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }
}