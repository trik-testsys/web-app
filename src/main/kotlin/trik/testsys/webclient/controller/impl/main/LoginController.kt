package trik.testsys.webclient.controller.impl.main

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.security.login.LoginProcessor
import trik.testsys.webclient.util.addPopupMessage
import trik.testsys.webclient.util.logger.TrikLogger

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
@Controller
@RequestMapping("/login")
class LoginController(
    private val loginProcessor: LoginProcessor
) {

    @GetMapping
    fun loginGet() = LOGIN_PAGE

    @PostMapping
    fun loginPost(
        @RequestParam(required = true) accessToken: String,
        redirectAttributes: RedirectAttributes
    ): String {
        loginProcessor.setCredentials(accessToken)

        val isLoggedIn = loginProcessor.login()
        if (isLoggedIn) return "redirect:${RedirectController.REDIRECT_PATH}"

        redirectAttributes.addPopupMessage("Некорретный Код-доступа. Попробуйте еще раз.")
        return "redirect:/$LOGIN_PAGE"
    }

    companion object {

        private val logger = TrikLogger(this::class.java)

        internal const val LOGIN_PAGE = "login"
        internal const val LOGIN_PATH = "/login"
    }
}