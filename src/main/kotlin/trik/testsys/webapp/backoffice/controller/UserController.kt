package trik.testsys.webapp.backoffice.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.ui.Model
import jakarta.servlet.http.HttpSession
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import trik.testsys.webapp.backoffice.data.service.impl.AccessTokenService
import trik.testsys.webapp.backoffice.controller.menu.MenuBuilder
import trik.testsys.webapp.backoffice.data.service.UserService

@Controller
@RequestMapping("/user")
class UserController(
    private val accessTokenService: AccessTokenService,
    private val userService: UserService,
    private val menuBuilder: MenuBuilder
) {

    @GetMapping
    fun getUserPage(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = (session.getAttribute("accessToken") as? String)?.let {
            accessTokenService.findByValue(it)
        } ?: run {
            redirectAttributes.addFlashAttribute("message", "Пожалуйста, войдите в систему.")
            return "redirect:/login"
        }

        val user = accessToken.user

        // Build dynamic menu from privileges
        val sections = menuBuilder.buildFor(user)

        model.addAttribute("hasActiveSession", true)
        model.addAttribute("user", user)
        model.addAttribute("menuSections", sections)
        return "user"
    }

    @PostMapping("/name")
    fun updateName(
        @RequestParam("name") newName: String?,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = (session.getAttribute("accessToken") as? String)?.let {
            accessTokenService.findByValue(it)
        } ?: run {
            redirectAttributes.addFlashAttribute("message", "Пожалуйста, войдите в систему.")
            return "redirect:/login"
        }

        val user = accessToken.user ?: run {
            redirectAttributes.addFlashAttribute("message", "Пользователь не найден.")
            return "redirect:/login"
        }

        val trimmed = (newName ?: "").trim()
        if (trimmed.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Псевдоним не может быть пустым.")
            return "redirect:/user"
        }

        userService.updateName(user, trimmed)
        redirectAttributes.addFlashAttribute("message", "Псевдоним успешно обновлен.")
        return "redirect:/user"
    }
}