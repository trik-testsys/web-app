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
import trik.testsys.webapp.backoffice.service.UserEmailService
import trik.testsys.webapp.backoffice.utils.addHasActiveSession
import trik.testsys.webapp.backoffice.utils.addMessage

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Controller
@RequestMapping("/user/mail")
class UserMailController(
    private val userEmailService: UserEmailService
) : AbstractUserController() {

    @PostMapping("/update")
    fun update(
        @RequestParam("email") newEmail: String,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val user = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        val trimmed = newEmail.trim()
        if (trimmed.isEmpty()) {
            redirectAttributes.addMessage("Почта не может быть пустой.")
            return "redirect:/user"
        }

        val existingUser = userService.findByEmail(newEmail)
        if (existingUser != null) {
            redirectAttributes.addMessage("Почта уже используется в системе")
            return "redirect:/user"
        }

        userEmailService.sendVerificationToken(user, trimmed)
        userService.updateEmail(user, trimmed)
        redirectAttributes.addMessage("Почта успешно обновлена. Для подтверждения скопируйте код, отправленный на указанную почту, и вставьте его в соседнее поле.")
        return "redirect:/user"
    }

    @PostMapping("/remove")
    fun remove(
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val user = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (user.email == null) {
            redirectAttributes.addMessage("К пользователю не привязана почта")
            return "redirect:/user"
        }

        userEmailService.sendVerificationToken(user, null)

        user.requestedEmailDetach = true
        userService.save(user)

        redirectAttributes.addMessage("Для открепления почты скопируйте код, отправленный на нее, и вставьте его в соседнее поле.")
        return "redirect:/user"
    }

    @PostMapping("/resend")
    fun resendToken(
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val user = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (user.email == null) {
            redirectAttributes.addMessage("К пользователю не привязана почта")
            return "redirect:/user"
        }

        if (user.requestedEmailDetach) {
            userEmailService.sendVerificationToken(user, null)
        } else {
            userEmailService.sendVerificationToken(user, user.email)
        }

        redirectAttributes.addMessage("Код отправлен повторно")
        return "redirect:/user"
    }

    @PostMapping("/verify")
    fun verify(
        @RequestParam verificationToken: String,
        session: HttpSession,
        redirectAttributes: RedirectAttributes,
        model: Model,
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val user = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        val verified = userEmailService.verify(user, verificationToken.trim())
        val message = if (verified) {
            if (user.email == null) "Почта откреплена"
            else "Почта подтверждена"
        } else {
            redirectAttributes.addFlashAttribute("isWarning", true)
            "Неверный код подтверждения"
        }

        redirectAttributes.addMessage(message)
        model.addHasActiveSession(session)

        return "redirect:/user"
    }

    @GetMapping("/restore")
    fun getRestorePage(session: HttpSession,model: Model): String {
        model.addHasActiveSession(session)
        model.addAttribute("tokenSent", null)


        return "token-restore"
    }

    @PostMapping("/restore")
    fun restoreEmail(
        @RequestParam("email") email: String,
        session: HttpSession,
        model: Model,
    ): String {
        val tokenSent = userEmailService.sendAccessToken(email.trim())

        model.addHasActiveSession(session)
        model.addAttribute("tokenSent", tokenSent)

        return "token-restore"
    }
}