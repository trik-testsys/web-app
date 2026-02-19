package trik.testsys.webapp.backoffice.service.startup.runner.impl

import jakarta.persistence.PostLoad
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.AccessToken
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.service.SuperUserService
import trik.testsys.webapp.backoffice.data.service.UserGroupService
import trik.testsys.webapp.backoffice.data.service.UserService
import trik.testsys.webapp.backoffice.data.service.impl.AccessTokenService
import trik.testsys.webapp.core.service.startup.AbstractStartupRunner
import java.io.File

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
@Order(0)
class SuperUserCreator(
    @Value("\${trik.testsys.superuser.name}")
    private val name: String,
    @Value("\${trik.testsys.superuser.accessToken.value}")
    private val accessToken: String,
    @Value("\${trik.testsys.superuser.accessToken.storeDir}")
    private val storeDirName: String,

    private val superUserService: SuperUserService,
    private val userService: UserService,
    private val accessTokenService: AccessTokenService,
    private val userGroupService: UserGroupService
) : AbstractStartupRunner() {

    private val storeDir = File(storeDirName)

    @PostLoad
    fun init() {
        if (!storeDir.exists()) {
            storeDir.mkdir()
        }
    }

    override suspend fun execute() = createSuperUser().storeToken()

    private fun AccessToken.storeToken() {
        val file = File("$storeDirName/$STORE_FILE_NAME")

        if (file.exists()) return
        file.createNewFile()
        file.writeText("${this.value!!}\n")

        logger.info("Stored access token in ${file.path}")
    }

    private fun createSuperUser(): AccessToken {
        val superUsers = superUserService.findAllSuperUser(isAllUserSuperUser = true).sortedBy { it.id }
        val token: AccessToken
        if (superUsers.isNotEmpty()) {
            token = superUsers.first().accessToken!!
            logger.info("Super User already exists. Skipping runner.\n\n Access token: ${token.value}\n")
            return token
        }

        token = if (accessToken.trim().isEmpty()) {
            accessTokenService.generate()
        } else AccessToken().also {
            it.value = accessToken
        }

        val user = User().also {
            it.name = name
            it.accessToken = token
            it.privileges.addAll(setOf(User.Privilege.SUPER_USER, User.Privilege.GROUP_ADMIN))
            it.isAllUserSuperUser = true
        }
        val persisted = userService.save(user)

        // Ensure default PUBLIC group exists owned by Super User
        userGroupService.getOrCreateDefaultGroup(persisted)

        logger.info("Created new Super User(id=${persisted.id}, name=$name).\n\n Access token: ${token.value}\n")
        return token
    }

    companion object {

        private const val STORE_FILE_NAME = ".access-token"
    }
}