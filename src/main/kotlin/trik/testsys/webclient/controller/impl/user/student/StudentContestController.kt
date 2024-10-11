package trik.testsys.webclient.controller.impl.user.student

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.impl.main.LoginController
import trik.testsys.webclient.controller.impl.user.student.StudentContestsController.Companion.CONTESTS_PAGE
import trik.testsys.webclient.controller.impl.user.student.StudentContestsController.Companion.CONTESTS_PATH
import trik.testsys.webclient.controller.impl.user.student.StudentMainController.Companion.STUDENT_PAGE
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.entity.user.impl.Student
import trik.testsys.webclient.service.entity.user.impl.StudentService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.addPopupMessage
import trik.testsys.webclient.view.impl.StudentContestView.Companion.toStudentView
import trik.testsys.webclient.view.impl.StudentView

@Controller
@RequestMapping(StudentContestController.CONTEST_PATH)
class StudentContestController(
    loginData: LoginData,

    private val studentService: StudentService,
) : AbstractWebUserController<Student, StudentView, StudentService>(loginData) {

    override val mainPage = CONTEST_PAGE

    override val mainPath = CONTEST_PATH

    override fun Student.toView(timeZoneId: String?) = TODO()

    @PostMapping("/start/{contestId}")
    fun contestStart(
        @PathVariable contestId: Long,
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        if (!webUser.group.isContestAvailable(contestId)) {
            redirectAttributes.addPopupMessage("Тур c ID '$contestId' не доступен.")
            return "redirect:$CONTESTS_PATH"
        }

        val contest = webUser.group.contests.first { it.id == contestId }
        if (!contest.isGoingOn()) {
            redirectAttributes.addPopupMessage("Тур '$contest.name' не доступен.")
            return "redirect:$CONTESTS_PATH"
        }

        if (webUser.startTimesByContestId.keys.contains(contest.id)) {
            return "redirect:$CONTEST_PATH/$contestId"
        }

        webUser.startContest(contest)
        studentService.save(webUser)

        redirectAttributes.addPopupMessage("Тур '${contest.name}' начат.")

        return "redirect:$CONTEST_PATH/$contestId"
    }

    @GetMapping("/{contestId}")
    fun contestGet(
        @PathVariable contestId: Long,
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        if (!webUser.group.isContestAvailable(contestId)) {
            redirectAttributes.addPopupMessage("Тур c ID '$contestId' не доступен.")
            return "redirect:$CONTESTS_PATH"
        }

        val contest = webUser.group.contests.first { it.id == contestId }
        if (!contest.isGoingOn()) {
            redirectAttributes.addPopupMessage("Тур '$contest.name' не доступен.")
            return "redirect:$CONTESTS_PATH"
        }

        model.addAttribute(CONTEST_ATTR, contest.toStudentView(timezone, webUser.lastTime(contest)))

        return CONTEST_PAGE
    }


    companion object {

        const val CONTEST_PATH = "$CONTESTS_PATH/contest"
        const val CONTEST_PAGE = "$STUDENT_PAGE/contest"

        const val CONTEST_ATTR = "contest"

        fun Group.isContestAvailable(contestId: Long) = contests.any { it.id == contestId }
    }
}