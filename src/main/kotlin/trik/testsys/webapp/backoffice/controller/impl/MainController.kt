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
import trik.testsys.webapp.backoffice.data.service.impl.StudentGroupTokenService
import trik.testsys.webapp.backoffice.data.service.StudentGroupService
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.utils.SESSION_ACCESS_TOKEN
import trik.testsys.webapp.backoffice.utils.addHasActiveSession
import trik.testsys.webapp.backoffice.utils.addMessage

@Controller
class MainController(
    private val accessTokenService: AccessTokenService,
    private val regTokenService: RegTokenService,
    private val viewerService: ViewerService,
    private val userService: UserService,
    private val studentGroupTokenService: StudentGroupTokenService,
    private val studentGroupService: StudentGroupService
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
        // Try Admin registration via viewer RegToken first
        regTokenService.findByValue(provided)?.let { token ->
            val viewer = token.viewer ?: run {
                redirectAttributes.addMessage("Неверный Код-доступа.")
                return "redirect:/login"
            }

            val newUser = viewerService.createAdmin(viewer, name) ?: run {
                redirectAttributes.addMessage("Ошибка")
                return "redirect:/login"
            }

            request.setAttribute("accessToken", newUser.accessToken?.value)
            return "forward:/login"
        }

        // Try Student registration via StudentGroupToken
        val stgToken = studentGroupTokenService.findByValue(provided) ?: run {
            redirectAttributes.addMessage("Неверный Код-доступа.")
            return "redirect:/login"
        }

        val group = stgToken.studentGroup ?: run {
            redirectAttributes.addMessage("Неверный Код-доступа.")
            return "redirect:/login"
        }

        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            redirectAttributes.addMessage("Псевдоним не может быть пустым.")
            return "redirect:/login"
        }

        val accessToken = accessTokenService.generate()
        val student = User().also {
            it.accessToken = accessToken
            it.name = trimmedName
            it.privileges.add(User.Privilege.STUDENT)
        }

        val persisted = userService.save(student)
        accessToken.user = persisted
        // Add student to the StudentGroup; service will also add to admin owner's user groups
        studentGroupService.addMember(group, persisted)

        request.setAttribute("accessToken", persisted.accessToken?.value)
        return "forward:/login"
    }

    companion object {
        private const val SESSION_USER_ID = "userId"
    }
}