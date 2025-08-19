package trik.testsys.webapp.backoffice.controller.impl

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webapp.backoffice.data.service.UserService
import trik.testsys.webapp.backoffice.data.service.ViewerService
import trik.testsys.webapp.backoffice.data.service.impl.AccessTokenService
import trik.testsys.webapp.backoffice.data.service.impl.RegTokenService
import trik.testsys.webapp.backoffice.utils.SESSION_ACCESS_TOKEN
import trik.testsys.webapp.backoffice.utils.addHasActiveSession
import trik.testsys.webapp.backoffice.utils.addMessage

@Controller
class MainController(
    private val accessTokenService: AccessTokenService,
    private val regTokenService: RegTokenService,
    private val viewerService: ViewerService,
    private val userService: UserService
) {

    @GetMapping("/")
    fun mainPage(model: Model, session: HttpSession): String {
        model.addHasActiveSession(session)
        return "main"
    }

    @GetMapping("/login")
    fun loginPage(model: Model, session: HttpSession): String {
        model.addHasActiveSession(session)
        return "login"
    }

    @GetMapping("/reg")
    fun regPage(model: Model, session: HttpSession): String {
        model.addHasActiveSession(session)
        return "reg"
    }

    @PostMapping("/login")
    fun login(
        @RequestParam("accessToken", required = false) accessTokenValue: String?,
        request: HttpServletRequest,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val provided = (accessTokenValue ?: (request.getAttribute("accessToken") as? String) ?: "").trim()
        val token = accessTokenService.findByValue(provided)

        val user = token?.user ?: run {
            redirectAttributes.addMessage("Неверный Код-доступа.")
            return "redirect:/login"
        }

        userService.updateLastLoginAt(user)

        session.setAttribute(SESSION_USER_ID, user.id)
        session.setAttribute(SESSION_ACCESS_TOKEN, token.value)
        return "redirect:/user"
    }

    @PostMapping("/logout")
    fun logout(session: HttpSession): String {
        session.invalidate()
        return "redirect:/"
    }

    @PostMapping("/reg")
    fun register(
        @RequestParam("regToken") regTokenValue: String,
        @RequestParam("name") name: String,
        request: HttpServletRequest,
        redirectAttributes: RedirectAttributes
    ): String {
        val provided = regTokenValue.trim()
        val token = regTokenService.findByValue(provided) ?: run {
                redirectAttributes.addMessage("Неверный Код-доступа.")
                return "redirect:/login"
            }

        val viewer = token.viewer ?: run {
            redirectAttributes.addMessage("Неверный Код-доступа.")
            return "redirect:/login"
        }

        val newUser = viewerService.createAdmin(viewer, name) ?: run {
            redirectAttributes.addMessage("Ошибка")
            return "redirect:/login"
        }

        // Forward POST to /login with accessToken as request attribute to avoid exposing it in URL
        request.setAttribute("accessToken", newUser.accessToken?.value)
        return "forward:/login"
    }

    companion object {
        private const val SESSION_USER_ID = "userId"
    }
}