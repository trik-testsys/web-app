package trik.testsys.webapp.backoffice.data.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.entity.impl.UserGroup
import trik.testsys.webapp.backoffice.data.repository.UserGroupRepository
import trik.testsys.webapp.backoffice.data.service.UserGroupService
import trik.testsys.webapp.core.data.service.AbstractService
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
class UserGroupServiceImpl :
    AbstractService<UserGroup, UserGroupRepository>(),
    UserGroupService {

    override fun addMember(userGroup: UserGroup, user: User): Boolean {
        val managed = try {
            if (userGroup.isNew()) userGroup else getById(userGroup.id!!)
        } catch (_: NoSuchElementException) {
            // Fallback to resolving by default flag to avoid failures with detached/stale ids during startup
            repository.findByDefaultGroupTrue() ?: userGroup
        }

        when (user) {
            managed.owner,
            in managed.activeMembers -> {
                logger.warn("Could not add already membered user(id=${user.id}) to userGroup(id=${managed.id}).")
                return false
            }
            else -> {
                logger.debug("Adding user(id=${user.id}) to userGroup(id=${managed.id})")
                managed.addMember(user)
            }
        }

        if (user.privileges.contains(User.Privilege.VIEWER)) {
            val allAdmins = user.managedAdmins
            managed.addMembers(allAdmins)

            val allStudents = allAdmins
                .flatMap { it.ownedStudentGroups }
                .flatMap { it.members }
            managed.addMembers(allStudents)
        } else if (user.privileges.contains(User.Privilege.ADMIN)) {
            val allStudents = user.ownedStudentGroups
                .flatMap { it.members }
            managed.addMembers(allStudents)
        }

        save(managed)
        return true
    }

    override fun findByOwner(owner: User): Set<UserGroup> = repository.findByOwner(owner)

    override fun findByMember(member: User): Set<UserGroup> = repository.findByMembersContaining(member)

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun create(owner: User, name: String, info: String?): UserGroup? {
        if (name.isBlank()) {
            logger.warn("Could not create user group: empty name")
            return null
        }
        val group = UserGroup().apply {
            this.owner = owner
            this.name = name
            this.info = info
        }
        return save(group)
    }

    override fun removeMember(userGroup: UserGroup, user: User): Boolean {
        val managed = try {
            if (userGroup.isNew()) userGroup else getById(userGroup.id!!)
        } catch (_: NoSuchElementException) {
            repository.findByDefaultGroupTrue() ?: userGroup
        }
        return when (user) {
            managed.owner -> {
                logger.warn("Could not remove owner(id=${user.id}) from userGroup(id=${managed.id}) members.")
                false
            }
            in managed.activeMembers.takeIf { managed.defaultGroup } ?: emptySet() -> {
                logger.warn("Could not remove user(id=${user.id}) from default userGroup(id=${managed.id}).")
                false
            }
            !in managed.activeMembers -> {
                logger.warn("Could not remove user(id=${user.id}) from not membered userGroup(id=${managed.id}).")
                false
            }
            else -> {
                logger.debug("Removing member(id=${user.id}) from userGroup(id=${managed.id}).")
                managed.removeMember(user)
                save(managed)
                true
            }
        }
    }

    override fun getOrCreateDefaultGroup(owner: User): UserGroup {
        val existing = repository.findByDefaultGroupTrue()
        if (existing != null) return existing
        val group = UserGroup().apply {
            this.owner = owner
            this.name = "Публичная"
            this.info = "Группа каждого пользователя по умолчанию."
            this.defaultGroup = true
        }
        return save(group)
    }

    override fun getDefaultGroup(): UserGroup? = repository.findByDefaultGroupTrue()

    companion object {

        private val logger = LoggerFactory.getLogger(UserGroupServiceImpl::class.java)
    }
}