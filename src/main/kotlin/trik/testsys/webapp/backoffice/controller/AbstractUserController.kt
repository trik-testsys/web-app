package trik.testsys.webapp.backoffice.controller

import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.ui.Model
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webapp.backoffice.data.entity.impl.AccessToken
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.service.UserService
import trik.testsys.webapp.backoffice.data.service.impl.AccessTokenService
import trik.testsys.webapp.backoffice.service.menu.MenuBuilder
import trik.testsys.webapp.backoffice.utils.addMessage
import trik.testsys.webapp.backoffice.utils.addHasActiveSession
import trik.testsys.webapp.backoffice.utils.addSections
import trik.testsys.webapp.backoffice.utils.addUser

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
            redirectAttributes.addMessage("Пожалуйста, войдите в систему.")
            return null
        }

        return accessToken
    }

    protected fun getUser(accessToken: AccessToken, redirectAttributes: RedirectAttributes): User? {
        accessToken.user ?: run {
            redirectAttributes.addMessage("Пользователь не найден.")
            return null
        }

        val user = accessToken.user ?: error("UNDEFINED")
        if (user.isRemoved) {
            redirectAttributes.addMessage("Доступ запрещён.")
            return null
        }

        return user
    }

    /**
     * Populates common model attributes for authenticated pages.
     */
    protected fun setupModel(model: Model, session: HttpSession, user: User) {
        model.apply {
            addHasActiveSession(session)
            addUser(user)
            addSections(menuBuilder.buildFor(user))
        }
    }

    companion object {

        const val ACCESS_TOKEN = "accessToken"
    }
}