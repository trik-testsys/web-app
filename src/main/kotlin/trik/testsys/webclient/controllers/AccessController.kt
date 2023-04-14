package trik.testsys.webclient.controllers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView

import trik.testsys.webclient.services.*

@RestController
class AccessController {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    private lateinit var webUserService: WebUserService

    @Autowired
    private lateinit var superUserService: SuperUserService

    @Autowired
    private lateinit var adminService: AdminService

    @Autowired
    private lateinit var studentService: StudentService

    @Autowired
    private lateinit var groupService: GroupService

    @GetMapping("/access")
    fun getAccess(@RequestParam accessToken: String, model: Model): Any {
        logger.info("[${accessToken.padStart(80)}]: Client trying to access service.")

        groupService.getGroupByAccessToken(accessToken)?.let {
            logger.info("[${accessToken.padStart(80)}]: Client is a new student.")
            return RedirectView("/v1/student/registration?groupAccessToken=$accessToken")
        }

        val webUser = webUserService.getWebUserByAccessToken(accessToken) ?: run {
            logger.info("[${accessToken.padStart(80)}]: Client is not authorized.")
            return model
        }

        superUserService.getSuperUserByWebUser(webUser)?.let {
            logger.info("[${accessToken.padStart(80)}]: Client is a super user.")
            return RedirectView("/superuser?accessToken=$accessToken")
        }

        adminService.getAdminByWebUser(webUser)?.let {
            logger.info("[${accessToken.padStart(80)}]: Client is an admin.")
            return RedirectView("/v1/admin?accessToken=$accessToken")
        }

        studentService.getStudentByWebUser(webUser)?.let {
            logger.info("[${accessToken.padStart(80)}]: Client is a student.")
            return RedirectView("/v1/student?accessToken=$accessToken")
        }

        return model
    }

    @GetMapping("/")
    fun get(model: Model): Model {
        return model
    }
}