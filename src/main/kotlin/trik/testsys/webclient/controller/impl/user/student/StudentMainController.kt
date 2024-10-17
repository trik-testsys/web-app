package trik.testsys.webclient.controller.impl.user.student

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import trik.testsys.webclient.controller.user.AbstractWebUserMainController
import trik.testsys.webclient.entity.user.impl.Student
import trik.testsys.webclient.service.entity.user.impl.StudentService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.view.impl.StudentView


@Controller
@RequestMapping(StudentMainController.STUDENT_PATH)
class StudentMainController(
    loginData: LoginData,
) : AbstractWebUserMainController<Student, StudentView, StudentService>(loginData) {

    override val mainPath = STUDENT_PATH

    override val mainPage = STUDENT_PAGE

    override fun Student.toView(timeZoneId: String?) = StudentView(
        id = this.id,
        name = this.name,
        accessToken = this.accessToken,
        creationDate = this.creationDate?.atTimeZone(timeZoneId),
        lastLoginDate = this.lastLoginDate?.atTimeZone(timeZoneId),
        additionalInfo = this.additionalInfo,
        group = this.group
    )

    companion object {

        internal const val STUDENT_PATH = "/student"
        internal const val STUDENT_PAGE = "student"
    }
}