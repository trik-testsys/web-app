package trik.testsys.webclient.controller.impl.user.judge

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.impl.main.LoginController.Companion.LOGIN_PATH
import trik.testsys.webclient.controller.impl.user.judge.JudgeMainController.Companion.JUDGE_PAGE
import trik.testsys.webclient.controller.impl.user.judge.JudgeMainController.Companion.JUDGE_PATH
import trik.testsys.webclient.controller.impl.user.judge.JudgeStudentsController.Companion.STUDENTS_PATH
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.entity.user.impl.Judge
import trik.testsys.webclient.service.entity.user.impl.JudgeService
import trik.testsys.webclient.service.entity.user.impl.StudentService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.view.impl.JudgeView
import trik.testsys.webclient.view.impl.StudentFilter
import trik.testsys.webclient.view.impl.StudentView
import trik.testsys.webclient.view.impl.StudentView.Companion.toView

@Controller
@RequestMapping(STUDENTS_PATH)
class JudgeStudentsController(
    loginData: LoginData,

    private val studentService: StudentService
) : AbstractWebUserController<Judge, JudgeView, JudgeService>(loginData) {

    override val mainPath = JUDGE_PATH

    override val mainPage = JUDGE_PAGE

    override fun Judge.toView(timeZoneId: String?) = TODO()

    @GetMapping
    fun studentsGetFiltered(
        @ModelAttribute("studentFilter") filter: StudentFilter,
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        if (filter.isEmpty()) {
            model.addAttribute(STUDENT_FILTER_ATTR, StudentFilter.empty())
            model.addAttribute(STUDENTS_ATTR, emptyList<StudentView>())

            return STUDENTS_PAGE
        }

        model.addAttribute(STUDENT_FILTER_ATTR, filter)

        val students = studentService.findAll().asSequence()
            .filter { filter.studentId?.let { studentId -> it.id == studentId } ?: true }
            .filter { filter.groupId?.let { groupId -> it.group.id == groupId } ?: true }
            .filter { filter.adminId?.let { adminId -> it.group.admin.id == adminId } ?: true }
            .filter { filter.contestId?.let { contestId -> it.group.contests.any { contest -> contest.id == contestId } } ?: true }
            .filter { filter.solutionId?.let { solutionId ->  it.solutions.any { solution -> solution.id == solutionId } } ?: true }
            .filter { filter.taskId?.let { taskId -> it.solutions.any { solution -> solution.task.id == taskId } } ?: true }
            .toList()

        if (students.isEmpty()) {
            model.addAttribute("message", "По заданному фильтру Участников не найдено")

            model.addAttribute(STUDENTS_ATTR, emptyList<StudentView>())
            return STUDENTS_PAGE
        }

        model.addAttribute("message", "Найдено ${students.size} Участников")
        model.addAttribute(STUDENTS_ATTR, students.map { it.toView(timezone) })

        return STUDENTS_PAGE
    }

    companion object {

        const val STUDENTS_PATH = "$JUDGE_PATH/students"
        const val STUDENTS_PAGE = "$JUDGE_PAGE/students"

        const val STUDENTS_ATTR = "students"

        const val STUDENT_FILTER_ATTR = "studentFilter"
    }
}