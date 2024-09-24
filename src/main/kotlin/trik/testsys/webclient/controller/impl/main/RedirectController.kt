package trik.testsys.webclient.controller.impl.main

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.View
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.enums.UserType
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.service.security.UserValidator
import trik.testsys.webclient.util.addInvalidAccessTokenMessage
import trik.testsys.webclient.util.addSessionExpiredMessage
import javax.servlet.http.HttpServletRequest

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
@Controller
@RequestMapping(RedirectController.REDIRECT_PATH)
class RedirectController(
    private val loginData: LoginData,
    private val userValidator: UserValidator
) {

    @GetMapping
    fun redirectGet(
        request: HttpServletRequest,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = loginData.accessToken ?: run {
            redirectAttributes.addSessionExpiredMessage()
            return "redirect:${LoginController.LOGIN_PATH}"
        }
        val user = userValidator.validateExistence(accessToken) ?: run {
            redirectAttributes.addInvalidAccessTokenMessage()
            return "redirect:${LoginController.LOGIN_PATH}"
        }

        request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT)
        return when (user.type) {
            UserType.ADMIN -> "redirect:/admin/login"
            UserType.DEVELOPER -> "redirect:/developer/login"
            UserType.JUDGE -> "redirect:/judge/login"
            UserType.STUDENT -> "redirect:/student/login"
            UserType.SUPER_USER -> "redirect:/superuser/login"
            UserType.VIEWER -> "redirect:/viewer/login"
        }
    }

    companion object {

        internal const val REDIRECT_PATH = "/redirect"
    }
}