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

    override fun addMember(userGroup: UserGroup, user: User) = when (user) {
        userGroup.owner,
        in userGroup.members -> {
            logger.warn("Could not add already membered user(id=${user.id}) to userGroup(id=${userGroup.id}).")
            false
        }
        else -> {
            logger.debug("Adding user(id=${user.id}) to userGroup(id=${userGroup.id})")
            userGroup.members.add(user)
            save(userGroup)
            true
        }
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

    override fun removeMember(userGroup: UserGroup, user: User) = when (user) {
        userGroup.owner -> {
            logger.warn("Could not remove owner(id=${user.id}) from userGroup(id=${userGroup.id}) members.")
            false
        }
        !in userGroup.members -> {
            logger.warn("Could not remove user(id=${user.id}) from not membered userGroup(id=${userGroup.id}).")
            false
        }
        else -> {
            logger.debug("Removing member(id=${user.id}) from userGroup(id=${userGroup.id}).")

            userGroup.members.remove(user)
            save(userGroup)
            true
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(UserGroupServiceImpl::class.java)
    }
}