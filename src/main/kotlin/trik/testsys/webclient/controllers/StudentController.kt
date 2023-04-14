package trik.testsys.webclient.controllers

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import trik.testsys.webclient.enums.WebUserStatuses
import trik.testsys.webclient.models.ResponseMessage

import trik.testsys.webclient.services.GroupService
import trik.testsys.webclient.services.StudentService
import trik.testsys.webclient.services.WebUserService

@RestController
@RequestMapping("/v1/student")
class StudentController {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    private lateinit var studentService: StudentService

    @Autowired
    private lateinit var groupService: GroupService

    @Autowired
    private lateinit var webUserService: WebUserService

    @GetMapping("/registration")
    fun registration(@RequestParam groupAccessToken: String, model: Model): Any {
        logger.info("[${groupAccessToken.padStart(80)}]: Client trying to register.")

        val group = groupService.getGroupByAccessToken(groupAccessToken) ?: run {
            logger.info("[${groupAccessToken.padStart(80)}]: Invalid  group token.")

            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseMessage(403, "Invalid group token!"))
        }


        logger.info("[${groupAccessToken.padStart(80)}]: Group token is valid.")

        model.addAttribute("groupAccessToken", groupAccessToken)
        return model
    }

    @PostMapping("/create")
    fun createStudent(@RequestParam groupAccessToken: String, @RequestParam username: String, model: Model): Any {
        logger.info("[${groupAccessToken.padStart(80)}]: Client trying to create student.")

        val group = groupService.getGroupByAccessToken(groupAccessToken) ?: run {
            logger.info("[${groupAccessToken.padStart(80)}]: Invalid  group token.")

            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseMessage(403, "Invalid group token!"))
        }

        logger.info("[${groupAccessToken.padStart(80)}]: Group token is valid.")

        val webUser = webUserService.saveWebUser(username)
        logger.info("[${groupAccessToken.padStart(80)}]: Web user created.")

        val student = studentService.saveStudent(webUser, group)
        logger.info("[${groupAccessToken.padStart(80)}]: Student created.")

        model.addAttribute("id", student.id)
        model.addAttribute("username", username)
        model.addAttribute("accessToken", webUser.accessToken)
        return model
    }

    @GetMapping
    fun getAccess(@RequestParam accessToken: String, model: Model): Any {
        logger.info("[${accessToken.padStart(80)}]: Client trying to access student page.")

        val status = validateStudent(accessToken)
        if (status == WebUserStatuses.NOT_FOUND || status == WebUserStatuses.ADMIN) {
            logger.info("[${accessToken.padStart(80)}]: Client is not a student.")
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseMessage(403, "You are not a student!"))
        }

        logger.info("[${accessToken.padStart(80)}]: Client is a student.")
        val webUser = webUserService.getWebUserByAccessToken(accessToken)!!
        val student = studentService.getStudentByWebUser(webUser)!!

        model.addAttribute("id", student.id)
        model.addAttribute("username", webUser.username)
        model.addAttribute("accessToken", accessToken)
        model.addAttribute("groupName", student.group.name)
        return model
    }

    private fun validateStudent(accessToken: String): WebUserStatuses {
        val webUser = webUserService.getWebUserByAccessToken(accessToken) ?: run {
            return WebUserStatuses.NOT_FOUND
        }

        val student = studentService.getStudentByWebUser(webUser) ?: run {
            return WebUserStatuses.ADMIN
        }

        return WebUserStatuses.WEB_USER
    }
}