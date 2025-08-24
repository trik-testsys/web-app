package trik.testsys.webapp.backoffice.service.startup.runner.impl

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.AccessToken
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.service.UserGroupService
import trik.testsys.webapp.backoffice.data.service.UserService
import trik.testsys.webapp.backoffice.data.service.impl.AccessTokenService
import trik.testsys.webapp.core.service.startup.AbstractStartupRunner

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
@Order(0)
class SuperUserCreator(
    @Value("\${trik.testsys.superuser.name}")
    private val name: String,
    @Value("\${trik.testsys.superuser.accessToken}")
    private val accessToken: String,

    private val userService: UserService,
    private val accessTokenService: AccessTokenService,
    private val userGroupService: UserGroupService
) : AbstractStartupRunner() {

    override suspend fun execute() = createSuperUser()

    private fun createSuperUser() {
        val superUsers = userService.findAllSuperUser().sortedBy { it.id }
        if (superUsers.isNotEmpty()) {
            logger.info("Super User already exists. Skipping runner.\n\n Access token: ${superUsers.first().accessToken?.value}\n")
            return
        }
        val token = if (accessToken.trim().isEmpty()) {
            accessTokenService.generate()
        } else AccessToken().also {
            it.value = accessToken
        }

        val user = User().also {
            it.name = name
            it.accessToken = token
            it.privileges.addAll(setOf(User.Privilege.SUPER_USER, User.Privilege.GROUP_ADMIN))
        }
        val persisted = userService.save(user)

        // Ensure default PUBLIC group exists owned by Super User
        userGroupService.getOrCreateDefaultGroup(persisted)

        logger.info("Created new Super User(id=${persisted.id}, name=$name).\n\n Access token: ${user.accessToken?.value}\n")
    }
}