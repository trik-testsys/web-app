package trik.testsys.webclient.controllers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import trik.testsys.webclient.entities.WebUser
import trik.testsys.webclient.services.AdminService
import trik.testsys.webclient.services.SuperUserService
import trik.testsys.webclient.services.WebUserService
import java.io.File
import java.lang.ProcessBuilder.Redirect

@RestController
class AccessController {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    private lateinit var webUserService: WebUserService

    @Autowired
    private lateinit var superUserService: SuperUserService

    @Autowired
    private lateinit var adminService: AdminService

    @GetMapping("/access")
    fun getAccess(@RequestParam accessToken: String, model: Model): Any {
        logger.info("Client trying to access service.")

        val webUser = webUserService.getWebUserByAccessToken(accessToken) ?: run {
            logger.info("Client is not authorized.")
            return model
        }

        superUserService.getSuperUserByWebUser(webUser)?.let {
            return RedirectView("/superuser?accessToken=$accessToken")
        }

        adminService.getAdminByWebUser(webUser)?.let {
            return RedirectView("/v1/admin?accessToken=$accessToken")
        }

        return model
    }

    @GetMapping("/")
    fun get(model: Model): Model {
        return model
    }
}