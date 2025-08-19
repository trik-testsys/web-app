package trik.testsys.webapp.backoffice.controller

import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webapp.backoffice.data.entity.impl.AccessToken
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.service.UserService
import trik.testsys.webapp.backoffice.data.service.impl.AccessTokenService
import trik.testsys.webapp.backoffice.service.menu.MenuBuilder

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
abstract class AbstractUserController {

    @Autowired
    protected lateinit var accessTokenService: AccessTokenService

    @Autowired
    protected lateinit var userService: UserService

    @Autowired
    protected lateinit var menuBuilder: MenuBuilder

    protected fun getAccessToken(session: HttpSession, redirectAttributes: RedirectAttributes): AccessToken? {
        val accessToken = (session.getAttribute(ACCESS_TOKEN) as? String)?.let {
            accessTokenService.findByValue(it)
        } ?: run {
            redirectAttributes.addFlashAttribute("message", "Пожалуйста, войдите в систему.")
            return null
        }

        return accessToken
    }

    protected fun getUser(accessToken: AccessToken, redirectAttributes: RedirectAttributes): User? {
        accessToken.user ?: run {
            redirectAttributes.addFlashAttribute("message", "Пользователь не найден.")
            return null
        }

        return accessToken.user
    }

    companion object {

        const val ACCESS_TOKEN = "accessToken"
    }
}