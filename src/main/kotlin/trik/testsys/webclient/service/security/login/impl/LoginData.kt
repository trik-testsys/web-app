package trik.testsys.webclient.service.security.login.impl

import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.SessionScope
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.service.security.login.SessionData

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
@Service
@SessionScope
class LoginData : SessionData {

    var accessToken: AccessToken? = null

    override fun invalidate() {
        accessToken = null
    }
}