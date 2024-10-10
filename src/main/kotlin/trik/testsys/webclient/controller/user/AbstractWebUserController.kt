package trik.testsys.webclient.controller.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.core.repository.user.UserRepository
import trik.testsys.core.view.user.UserView
import trik.testsys.webclient.entity.user.WebUser
import trik.testsys.webclient.service.entity.user.WebUserService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.addExitMessage
import trik.testsys.webclient.util.addSessionExpiredMessage
import java.util.TimeZone

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
abstract class AbstractWebUserController<U : WebUser, V : UserView<U>, S : WebUserService<U, out UserRepository<U>>>(
    val loginData: LoginData
) {

    protected abstract val mainPath: String

    protected abstract val mainPage: String

    @Autowired
    protected lateinit var service: S

    protected abstract fun U.toView(timeZone: TimeZone): V

    /**
     * @author Roman Shishkin
     * @since 2.0.0
     **/
    protected fun LoginData.validate(redirectAttributes: RedirectAttributes): U? {
        val webUser = accessToken?.let { service.findByAccessToken(it) } ?: run {
            invalidate()
            redirectAttributes.addSessionExpiredMessage()
            return null
        }

        return webUser
    }

    /**
     * @author Roman Shishkin
     * @since 2.0.0
     **/
    protected fun String?.checkLogout(redirectAttributes: RedirectAttributes): Boolean {
        this ?: return false

        redirectAttributes.addExitMessage()
        return true
    }

    companion object {

        const val WEB_USER_ATTR = "webUser"
    }
}