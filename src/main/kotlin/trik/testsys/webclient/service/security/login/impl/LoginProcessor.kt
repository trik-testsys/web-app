package trik.testsys.webclient.service.security.login.impl

import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.RequestScope
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.service.security.UserValidator
import trik.testsys.webclient.service.security.login.SecurityProcessor

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
@Service
@RequestScope
class LoginProcessor(
    private val loginData: LoginData,
    private val userValidator: UserValidator
) : SecurityProcessor {

    private lateinit var accessToken: String

    fun login(): Boolean {
        val webUser = userValidator.validateExistence(accessToken) ?: return false

        loginData.accessToken = webUser.accessToken
        return true
    }

    fun setCredentials(accessToken: AccessToken) {
        this.accessToken = accessToken
    }
}