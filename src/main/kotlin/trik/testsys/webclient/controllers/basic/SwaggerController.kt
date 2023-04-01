package trik.testsys.webclient.controllers.basic

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView

@RestController
class SwaggerController {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/swagger")
    fun getSwagger(): RedirectView {
        logger.info("Client requested swagger documentation.")
        return RedirectView("/swagger-ui/index.html")
    }
}