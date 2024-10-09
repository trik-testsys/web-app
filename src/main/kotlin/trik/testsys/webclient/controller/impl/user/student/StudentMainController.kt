package trik.testsys.webclient.controller.impl.user.student

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import trik.testsys.webclient.controller.user.AbstractWebUserMainController
import trik.testsys.webclient.entity.user.impl.Student
import trik.testsys.webclient.service.entity.user.impl.StudentService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.view.impl.StudentView
import java.util.TimeZone


@Controller
@RequestMapping(StudentMainController.STUDENT_PATH)
class StudentMainController(
    loginData: LoginData,
) : AbstractWebUserMainController<Student, StudentView, StudentService>(loginData) {

    override val mainPath = STUDENT_PATH

    override val mainPage = STUDENT_PAGE

    override fun Student.toView(timeZone: TimeZone) = StudentView(
        id = this.id,
        name = this.name,
        accessToken = this.accessToken,
        creationDate = this.creationDate?.atTimeZone(timeZone),
        lastLoginDate = this.lastLoginDate?.atTimeZone(timeZone),
        additionalInfo = this.additionalInfo,
        group = this.group
    )

//    @ResponseBody
//    @GetMapping("/task/download")
//    fun downloadTask(
//        redirectAttributes: RedirectAttributes,
//        model: Model
//    ): Any {
//        validate(redirectAttributes) ?: return RedirectView(LoginController.LOGIN_PATH)
//        val file = File("/Users/shisha/Projects/Kotlin/trik-testsys-web-client2/Dockerfile")
//
//        val responseEntity = ResponseEntity.ok()
//            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"task.qrs\"")
//            .contentType(MediaType.APPLICATION_OCTET_STREAM)
//            .body(file.readBytes())
//
//        return responseEntity
//    }

    companion object {

        internal const val STUDENT_PATH = "/student"
        internal const val STUDENT_PAGE = "student"
    }
}