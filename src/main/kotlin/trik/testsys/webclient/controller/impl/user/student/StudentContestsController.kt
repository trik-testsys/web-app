package trik.testsys.webclient.controller.impl.user.student

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.impl.main.LoginController
import trik.testsys.webclient.controller.impl.user.student.StudentMainController.Companion.STUDENT_PAGE
import trik.testsys.webclient.controller.impl.user.student.StudentMainController.Companion.STUDENT_PATH
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.entity.user.impl.Student
import trik.testsys.webclient.service.entity.user.impl.StudentService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.view.impl.StudentContestView.Companion.toStudentView
import trik.testsys.webclient.view.impl.StudentView

@Controller
@RequestMapping(StudentContestsController.CONTESTS_PATH)
class StudentContestsController(
    loginData: LoginData
) : AbstractWebUserController<Student, StudentView, StudentService>(loginData) {

    override val mainPage = CONTESTS_PAGE

    override val mainPath = CONTESTS_PATH

    override fun Student.toView(timeZoneId: String?) = TODO()

    @GetMapping
    fun contestsGet(
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        model.addAttribute(CONTESTS_ATTR, webUser.group.contests.map { it.toStudentView(timezone, webUser.lastTime(it)) }.sortedBy { it.id })

        return CONTESTS_PAGE
    }

    companion object {

        const val CONTESTS_PATH = "$STUDENT_PATH/contests"
        const val CONTESTS_PAGE = "$STUDENT_PAGE/contests"

        const val CONTESTS_ATTR = "contests"
    }
}