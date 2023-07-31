package trik.testsys.webclient.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@RestController
@RequestMapping("\${app.testsys.api.prefix}/developer")
class DeveloperController @Autowired constructor(
//    private val developerService: DeveloperService,
//    private val webUserService: WebUserService,
) {

    @GetMapping("/test")
    fun test(): ModelAndView {
        val modelAndView = ModelAndView("developer")
        modelAndView.addObject("username", "Roman")
        modelAndView.addObject("accessToken", "ed30da0f75d595465d6977e2fd551d2026cc3ff66dd5bd958ac2a50807684cb7")
        println(modelAndView.model)
        return modelAndView
    }
}