package trik.testsys.webclient.controller.impl.main

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.addSessionActiveInfo

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
@Controller
@RequestMapping(MainController.MAIN_PATH)
class MainController(
    private val loginData: LoginData
) {

    @GetMapping
    fun mainGet(model: Model): String {
        loginData.accessToken?.let { model.addSessionActiveInfo() }

        return MAIN_PAGE
    }

    companion object {

        internal const val MAIN_PAGE = "main"
        internal const val MAIN_PATH = "/"
    }
}