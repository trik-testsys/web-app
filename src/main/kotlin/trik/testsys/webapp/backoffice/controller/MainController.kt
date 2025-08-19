package trik.testsys.webapp.backoffice.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.ui.Model
import jakarta.servlet.http.HttpSession
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webapp.backoffice.data.service.ViewerService
import trik.testsys.webapp.backoffice.data.service.impl.AccessTokenService
import trik.testsys.webapp.backoffice.data.service.impl.RegTokenService
import trik.testsys.webapp.backoffice.data.service.impl.UserServiceImpl
import java.time.Instant

@Controller
class MainController(
    private val accessTokenService: AccessTokenService,
    private val regTokenService: RegTokenService,
    private val viewerService: ViewerService,
    private val userService: UserServiceImpl
) {

    @GetMapping("/")
    fun getPage(model: Model, session: HttpSession): String {
        val hasActiveSession = session.getAttribute(SESSION_ACCESS_TOKEN) != null
        model.addAttribute("hasActiveSession", hasActiveSession)
        return "main"
    }

    @GetMapping("/login")
    fun getLoginPage(model: Model, session: HttpSession): String {
        val hasActiveSession = session.getAttribute(SESSION_ACCESS_TOKEN) != null
        model.addAttribute("hasActiveSession", hasActiveSession)
        return "login"
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

        if (token?.user == null) {
            redirectAttributes.addFlashAttribute("message", "Неверный Код-доступа.")
            return "redirect:/login"
        }

        // Update last login timestamp
        token.user!!.lastLoginAt = Instant.now()
        userService.save(token.user!!)

        session.setAttribute(SESSION_USER_ID, token.user!!.id)
        session.setAttribute(SESSION_ACCESS_TOKEN, token.value)
        return "redirect:/user"
    }

    @PostMapping("/logout")
    fun logout(session: HttpSession): String {
        session.invalidate()
        return "redirect:/"
    }

    @GetMapping("/reg")
    fun getRegPage(model: Model, session: HttpSession): String {
        val hasActiveSession = session.getAttribute(SESSION_ACCESS_TOKEN) != null
        model.addAttribute("hasActiveSession", hasActiveSession)
        return "reg"
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
                redirectAttributes.addFlashAttribute("message", "Неверный Код-доступа.")
                return "redirect:/login"
            }

        if (token.viewer == null) {
            redirectAttributes.addFlashAttribute("message", "Неверный Код-доступа.")
            return "redirect:/login"
        }

        val newUser = viewerService.createAdmin(token, name) ?: run {
            redirectAttributes.addFlashAttribute("message", "Ошибка")
            return "redirect:/login"
        }

        val accessToken = newUser.accessToken

        // Forward POST to /login with accessToken as request attribute to avoid exposing it in URL
        request.setAttribute("accessToken", accessToken?.value)
        return "forward:/login"
    }

    companion object {
        private const val SESSION_ACCESS_TOKEN = "accessToken"
        private const val SESSION_USER_ID = "userId"
    }
}