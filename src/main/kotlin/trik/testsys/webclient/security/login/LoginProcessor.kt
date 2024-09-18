package trik.testsys.webclient.security.login

import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.RequestScope
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.repository.user.WebUserRepository
import trik.testsys.webclient.security.SecurityProcessor

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
@Service
@RequestScope
class LoginProcessor(
    private val loginData: LoginData,
    private val webUserRepository: WebUserRepository
) : SecurityProcessor {

    private lateinit var accessToken: String

    fun login(): Boolean {
        val webUser = webUserRepository.findByAccessToken(accessToken) ?: return false

        loginData.webUser = webUser
        return true
    }

    fun setCredentials(accessToken: AccessToken) {
        this.accessToken = accessToken
    }
}