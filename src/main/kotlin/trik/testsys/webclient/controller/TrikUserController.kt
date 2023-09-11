package trik.testsys.webclient.controller

import org.springframework.web.servlet.ModelAndView

interface TrikUserController {

    fun getAccess(
        accessToken: String,
        modelAndView: ModelAndView
    ): ModelAndView
}