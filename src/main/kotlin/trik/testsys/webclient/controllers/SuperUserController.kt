package trik.testsys.webclient.controllers

import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import trik.testsys.webclient.enums.WebUserStatuses
import trik.testsys.webclient.models.AdminModel
import trik.testsys.webclient.services.SuperUserService
import trik.testsys.webclient.services.WebUserService
import trik.testsys.webclient.models.ResponseMessage
import trik.testsys.webclient.models.WebUserModel
import trik.testsys.webclient.services.AdminService


@RequestMapping("/superuser")
@RestController
class SuperUserController {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    private lateinit var superUserService: SuperUserService

    @Autowired
    private lateinit var webUserService: WebUserService

    @Autowired
    private lateinit var adminService: AdminService

    @ApiResponses(
        ApiResponse(code = 200, message = "Client is a super user.", response = ResponseMessage::class),
        ApiResponse(code = 403, message = "Client is not a super user.", response = ResponseMessage::class)
    )
    @GetMapping("/hello", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hello(@RequestParam accessToken: String): ResponseEntity<ResponseMessage> {
        logger.info("Client trying to access super user hello page.")

        val status = validateSuperUser(accessToken)
        if (status == WebUserStatuses.SUPER_USER) {
            logger.info("Client is a super user.")
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseMessage(200, "You are a superuser!"))
        }

        logger.info("Client is not a super user.")
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ResponseMessage(403, "You are not a superuser!"))
    }

    @ApiResponses(
        ApiResponse(code = 201, message = "New web user successfully created.", response = WebUserModel::class),
        ApiResponse(code = 403, message = "Client is not a super user.", response = ResponseMessage::class),
    )
    @PostMapping("/webUser/create", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createWebUser(@RequestParam accessToken: String, @RequestParam username: String): ResponseEntity<Any> {
        logger.info("Client trying to create web user.")

        val status = validateSuperUser(accessToken)
        if (status != WebUserStatuses.SUPER_USER) {
            logger.info("Client is not a super user.")
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseMessage(403, "You are not a superuser!"))
        }

        val webUser = webUserService.saveWebUser(username)
        logger.info("Web user created.")
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(webUser.toModel())
    }

    @ApiResponses(
        ApiResponse(code = 201, message = "Web user successfully raised to admin. New admin created.", response = AdminModel::class),
        ApiResponse(code = 403, message = "Client is not a super user.", response = ResponseMessage::class),
        ApiResponse(code = 404, message = "Web user not found.", response = ResponseMessage::class),
        ApiResponse(code = 409, message = "Web user is already an admin.", response = ResponseMessage::class)
    )
    @PostMapping("/webUser/raise", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun raiseWebUserToAdmin(@RequestParam accessToken: String, @RequestParam webUserId: Long): ResponseEntity<Any> {
        logger.info("Client trying to raise web user to admin.")

        val status = validateSuperUser(accessToken)
        if (status != WebUserStatuses.SUPER_USER) {
            logger.info("Client is not a super user.")
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseMessage(403, "You are not a superuser!"))
        }

        webUserService.getWebUserById(webUserId)
            ?: run {
                logger.info("Web user with id $webUserId not found.")
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseMessage(404, "Web user not found!"))
            }

        adminService.getAdminByWebUserId(webUserId)
            ?: run {
                logger.info("Raising web user to admin.")
                val admin = adminService.saveAdmin(webUserId)
                    ?: return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ResponseMessage(500, "Internal server error!"))

                return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(admin.toModel())
            }

        logger.info("Web user is already admin.")
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ResponseMessage(409, "Web user is already admin!"))
    }

    @ApiResponses(
        ApiResponse(code = 201, message = "New admin successfully created.", response = AdminModel::class),
        ApiResponse(code = 403, message = "Client is not a super user.", response = ResponseMessage::class),
    )
    @PostMapping("/admin/create", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createAdmin(@RequestParam accessToken: String, @RequestParam username: String): ResponseEntity<Any> {
        logger.info("Client trying to create admin.")

        val status = validateSuperUser(accessToken)
        if (status != WebUserStatuses.SUPER_USER) {
            logger.info("Client is not a super user.")
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseMessage(403, "You are not a superuser!"))
        }

        val webUser = webUserService.saveWebUser(username)
        val admin = adminService.saveAdmin(webUser)
            ?: return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseMessage(500, "Internal server error!"))

        logger.info("Admin created.")
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(admin.toModel())
    }


    private fun validateSuperUser(accessToken: String): WebUserStatuses {
        val webUser = webUserService.getWebUserByAccessToken(accessToken) ?: return WebUserStatuses.NOT_FOUND
        superUserService.getSuperUserByWebUserId(webUser.id!!) ?: return WebUserStatuses.WEB_USER

        return WebUserStatuses.SUPER_USER
    }
}