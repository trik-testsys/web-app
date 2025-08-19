package trik.testsys.webapp.backoffice.controller.impl.user

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webapp.backoffice.controller.AbstractUserController

@Controller
@RequestMapping("/user")
class UserController : AbstractUserController() {

    @GetMapping
    fun getUserPage(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val user = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

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
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val user = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

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