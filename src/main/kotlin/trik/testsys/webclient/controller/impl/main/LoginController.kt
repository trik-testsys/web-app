package trik.testsys.webclient.controller.impl.main

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.service.LogoService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.service.security.login.impl.LoginProcessor
import trik.testsys.webclient.util.addInvalidAccessTokenMessage
import trik.testsys.webclient.util.addSessionActiveInfo

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
@Controller
@RequestMapping("/login")
class LoginController(
    private val loginData: LoginData,
    private val loginProcessor: LoginProcessor,

    private val logoService: LogoService
) {

    @GetMapping
    fun loginGet(model: Model): String {
        loginData.accessToken?.let { model.addSessionActiveInfo() }

        val logos = logoService.getLogos()
        model.addAttribute("logos", logos)

        return LOGIN_PAGE
    }

    @PostMapping
    fun loginPost(
        @RequestParam(required = true) accessToken: String,
        redirectAttributes: RedirectAttributes
    ): String {
        loginProcessor.setCredentials(accessToken)

        val isLoggedIn = loginProcessor.login()
        if (isLoggedIn) return "redirect:${RedirectController.REDIRECT_PATH}"

        redirectAttributes.addInvalidAccessTokenMessage()
        return "redirect:/$LOGIN_PAGE"
    }

    companion object {

        internal const val LOGIN_PAGE = "login"
        internal const val LOGIN_PATH = "/login"
    }
}