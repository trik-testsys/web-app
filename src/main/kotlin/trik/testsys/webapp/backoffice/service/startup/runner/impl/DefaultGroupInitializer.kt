package trik.testsys.webapp.backoffice.service.startup.runner.impl

import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.repository.UserGroupRepository
import trik.testsys.webapp.backoffice.data.repository.UserRepository
import trik.testsys.webapp.backoffice.data.service.UserGroupService
import trik.testsys.webapp.backoffice.data.service.UserService
import trik.testsys.webapp.core.service.startup.AbstractStartupRunner

@Service
@Order(10)
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
class DefaultGroupInitializer(
    private val userRepository: UserRepository,
    private val userGroupRepository: UserGroupRepository,
//    private val userService: UserService,
    private val userGroupService: UserGroupService
) : AbstractStartupRunner() {

    override suspend fun execute() {
        initializeDefaultGroup()
    }

    private fun initializeDefaultGroup() {
        val groupAdmins = userRepository.findAll().filter { it.privileges.contains(User.Privilege.GROUP_ADMIN) }
        val owner = groupAdmins.firstOrNull() ?: run {
            logger.error("Where are no user in system, can not initialize PUBLIC UserGroup.")
            return
        }

        val defaultGroup = userGroupService.getOrCreateDefaultGroup(owner)

        val allUsers = userRepository.findAll().sortedBy { it.id }
        var added = 0
        allUsers.forEach { user ->
            if (userGroupService.addMember(defaultGroup, user)) {
                added++
            }
        }
        if (added > 0) {
            logger.info("Added $added users to the default PUBLIC group(id=${defaultGroup.id}).")
        }
    }
}