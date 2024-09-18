package trik.testsys.webclient.controller.user

import org.springframework.web.servlet.ModelAndView

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
interface TrikUserController {

    fun getAccess(
        accessToken: String,
        modelAndView: ModelAndView
    ): ModelAndView
}