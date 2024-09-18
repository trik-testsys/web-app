package trik.testsys.webclient.security.login

import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.SessionScope
import trik.testsys.webclient.entity.impl.user.WebUser
import trik.testsys.webclient.security.SessionData

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
@Component
@SessionScope
class LoginData : SessionData {

    var webUser: WebUser? = null

    override fun invalidate() {
        webUser = null
    }
}