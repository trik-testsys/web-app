package trik.testsys.webclient.security.login

import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.RequestScope
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.security.SecurityProcessor
import trik.testsys.webclient.service.impl.user.WebUserService
import java.time.LocalDateTime

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
@Service
@RequestScope
class LoginProcessor(
    private val loginData: LoginData,
    private val webUserService: WebUserService
) : SecurityProcessor {

    private lateinit var accessToken: String

    fun login(): Boolean {
        val webUser = webUserService.findByAccessToken(accessToken) ?: return false

        webUser.lastLoginDate = LocalDateTime.now()
        webUserService.save(webUser)

        loginData.webUser = webUser
        return true
    }

    fun setCredentials(accessToken: AccessToken) {
        this.accessToken = accessToken
    }
}