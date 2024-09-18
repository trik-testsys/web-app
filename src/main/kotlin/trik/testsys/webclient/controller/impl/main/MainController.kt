package trik.testsys.webclient.controller.impl.main

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
@Controller
@RequestMapping(MainController.MAIN_PATH)
class MainController {

    @GetMapping
    fun mainGet() = MAIN_PAGE

    companion object {

        internal const val MAIN_PAGE = "main"
        internal const val MAIN_PATH = "/"
    }
}