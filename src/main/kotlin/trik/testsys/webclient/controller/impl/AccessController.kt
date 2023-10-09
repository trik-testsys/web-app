package trik.testsys.webclient.controller.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.view.RedirectView
import trik.testsys.webclient.entity.impl.WebUser

import trik.testsys.webclient.service.impl.*
import trik.testsys.webclient.util.AvatarGenerator
import trik.testsys.webclient.util.logger.TrikLogger
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.util.Random

@RestController
@RequestMapping("\${app.testsys.api.prefix}")
class AccessController @Autowired constructor(
    private val webUserService: WebUserService,
    private val superUserService: SuperUserService,
    private val adminService: AdminService,
    private val studentService: StudentService,
    private val groupService: GroupService,
    private val developerService: DeveloperService,
    private val viewerService: ViewerService,
    private val avatarGenerator: AvatarGenerator
) {

    @GetMapping("/access")
    fun getAccess(
        @RequestParam accessToken: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): Any {
        logger.info(accessToken, "Client trying to access service.")

        groupService.getGroupByAccessToken(accessToken)?.let {
            logger.info(accessToken, "Client is a new student.")
            if (!it.isRegistrationOpen) {
                // TODO: Add message about closed registration.
                logger.info(accessToken, "Registration is closed.")
                return@let
            }
            redirectAttributes.addAttribute("groupAccessToken", accessToken)
            return RedirectView("$SERVER_PREFIX/registration")
//            return RedirectView("/v1/testsys/student/registration")
        }
        redirectAttributes.addAttribute("accessToken", accessToken)

        viewerService.getByAdminRegToken(accessToken)?.let {
            logger.info(accessToken, "Client is a new admin.")
            return RedirectView("$SERVER_PREFIX/registration")
//            return RedirectView("/v1/testsys/admin/registration")
        }

        val webUser = webUserService.getWebUserByAccessToken(accessToken) ?: run {
            logger.info(accessToken, "Client is not authorized.")
            return model
        }
        webUser.lastLoginDate = LocalDateTime.now(UTC).plusHours(3)
        webUserService.saveWebUser(webUser)

        superUserService.getSuperUserByWebUser(webUser)?.let {
            logger.info(accessToken, "Client is a super user.")
            return RedirectView("$SERVER_PREFIX/superuser")
//            return RedirectView("/v1/testsys/superuser")
        }

        adminService.getAdminByWebUser(webUser)?.let {
            logger.info(accessToken, "Client is an admin.")
            return RedirectView("$SERVER_PREFIX/admin")
//            return RedirectView("/v1/testsys/admin")
        }

        developerService.getByWebUser(webUser)?.let {
            logger.info(accessToken, "Client is a developer.")
            return RedirectView("$SERVER_PREFIX/developer")
//            return RedirectView("/v1/testsys/developer")
        }

        studentService.getByWebUser(webUser)?.let {
            logger.info(accessToken, "Client is a student.")
            val group = it.group
            if (!group.isAccessible) {
                //TODO: Add message about group is not accessible.
                logger.info(accessToken, "Group is not accessible.")
                return@let
            }
            return RedirectView("$SERVER_PREFIX/student")
//            return RedirectView("/v1/testsys/student")
        }

        viewerService.getByWebUser(webUser)?.let {
            logger.info(accessToken, "Client is a viewer.")

            return RedirectView("$SERVER_PREFIX/viewer")
//            return RedirectView("/v1/testsys/viewer")
        }

        return model
    }

    @GetMapping
    fun get(model: Model): Model {
        return model
    }

    companion object {
        private val logger = TrikLogger(this::class.java)
        private const val SERVER_PREFIX = "https://testsys.trikset.com/v1/testsys"
    }
}