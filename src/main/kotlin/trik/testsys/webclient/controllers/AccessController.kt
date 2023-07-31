package trik.testsys.webclient.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView

import trik.testsys.webclient.services.*
import trik.testsys.webclient.utils.logger.TrikLogger

@RestController
@RequestMapping("\${app.testsys.api.prefix}")
class AccessController @Autowired constructor(
    private val webUserService: WebUserService,
    private val superUserService: SuperUserService,
    private val adminService: AdminService,
    private val studentService: StudentService,
    private val groupService: GroupService,
    private val developerService: DeveloperService
) {

    @GetMapping("/access")
    fun getAccess(@RequestParam accessToken: String, model: Model): Any {
        logger.info(accessToken, "Client trying to access service.")

        groupService.getGroupByAccessToken(accessToken)?.let {
            logger.info(accessToken, "Client is a new student.")
//            return RedirectView("https://srv3.trikset.com:8843/v1/testsys/student/registration?groupAccessToken=$accessToken")
            return RedirectView("/v1/testsys/student/registration?groupAccessToken=$accessToken")
        }

        val webUser = webUserService.getWebUserByAccessToken(accessToken) ?: run {
            logger.info(accessToken, "Client is not authorized.")
            return model
        }

        superUserService.getSuperUserByWebUser(webUser)?.let {
            logger.info(accessToken, "Client is a super user.")
//            return RedirectView("https://srv3.trikset.com:8843/v1/testsys/superuser?accessToken=$accessToken")
            return RedirectView("/v1/testsys/superuser?accessToken=$accessToken")
        }

        adminService.getAdminByWebUser(webUser)?.let {
            logger.info(accessToken, "Client is an admin.")
//            return RedirectView("https://srv3.trikset.com:8843/v1/testsys/admin?accessToken=$accessToken")
            return RedirectView("/v1/testsys/admin?accessToken=$accessToken")
        }

        developerService.getByWebUser(webUser)?.let {
            logger.info(accessToken, "Client is a developer.")
            return RedirectView("/v1/testsys/developer?accessToken=$accessToken")
        }

        studentService.getByWebUser(webUser)?.let {
            logger.info(accessToken, "Client is a student.")
//            return RedirectView("https://srv3.trikset.com:8843/v1/testsys/student?accessToken=$accessToken")
            return RedirectView("/v1/testsys/student?accessToken=$accessToken")
        }

        return model
    }

    @GetMapping
    fun get(model: Model): Model {
        return model
    }

    companion object {
        private val logger = TrikLogger(this::class.java)
    }
}