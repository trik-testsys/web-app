package trik.testsys.webapp.backoffice.service.startup

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.AccessToken
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.service.UserService
import trik.testsys.webapp.backoffice.data.service.impl.AccessTokenService
import trik.testsys.webapp.core.service.startup.StartupRunner

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
class SuperUserCreator(
    @Value("\${trik.testsys.superuser.name}")
    private val name: String,
    @Value("\${trik.testsys.superuser.accessToken}")
    private val accessToken: String,

    private val userService: UserService,
    private val accessTokenService: AccessTokenService
) : StartupRunner {

    override suspend fun execute() = createSuperUser()

    private fun createSuperUser() {
        val superUsers = userService.findAllSuperUser().sortedBy { it.id }
        if (superUsers.isNotEmpty()) {
            logger.info("Super User already exists. Skipping runner.\n\n Access token: ${superUsers.first().accessToken?.value}\n")
            return
        }
        val token = if (accessToken.trim().isEmpty()) {
            accessTokenService.generate(null)
        } else AccessToken().also {
            it.value = accessToken
        }

        val user = User().also {
            it.name = name
            it.accessToken = token
            it.privileges.add(User.Privilege.SUPER_USER)
        }
        val persisted = userService.save(user)

        logger.info("Created new Super User(id=${persisted.id}, name=$name).\n\n Access token: ${user.accessToken?.value}\n")
    }

    companion object {

        private val logger = LoggerFactory.getLogger(SuperUserCreator::class.java)
    }
}