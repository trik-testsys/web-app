//package trik.testsys.webapp.backoffice.controller.impl.user.student
//
//import org.springframework.stereotype.Controller
//import org.springframework.ui.Model
//import org.springframework.web.bind.annotation.CookieValue
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.servlet.mvc.support.RedirectAttributes
//import trik.testsys.backoffice.controller.impl.main.LoginController
//import trik.testsys.backoffice.controller.impl.user.student.StudentMainController.Companion.STUDENT_PAGE
//import trik.testsys.backoffice.controller.impl.user.student.StudentMainController.Companion.STUDENT_PATH
//import trik.testsys.backoffice.controller.user.AbstractWebUserController
//import trik.testsys.backoffice.entity.user.impl.Student
//import trik.testsys.backoffice.service.entity.impl.ContestService
//import trik.testsys.backoffice.service.entity.user.impl.StudentService
//import trik.testsys.backoffice.service.security.login.impl.LoginData
//import trik.testsys.backoffice.view.impl.StudentContestView.Companion.toStudentView
//import trik.testsys.backoffice.view.impl.StudentView
//
//@Controller
//@RequestMapping(StudentContestsController.CONTESTS_PATH)
//class StudentContestsController(
//    loginData: LoginData,
//
//    private val contestService: ContestService
//) : AbstractWebUserController<Student, StudentView, StudentService>(loginData) {
//
//    override val mainPage = CONTESTS_PAGE
//
//    override val mainPath = CONTESTS_PATH
//
//    override fun Student.toView(timeZoneId: String?) = TODO()
//
//    @GetMapping
//    fun contestsGet(
//        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
//        redirectAttributes: RedirectAttributes,
//        model: Model
//    ): String {
//        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"
//
//        val groupContests = webUser.group.contests
//        val goingOnContests = groupContests.filter { !it.isOutdatedFor(webUser) && it.isGoingOn() }
//
//        model.addAttribute(CONTESTS_ATTR, goingOnContests.map { it.toStudentView(timezone, webUser.remainingTimeFor(it)) }.sortedBy { it.id })
//
//        val allContests = contestService.findAll()
//        val outdatedStudentContests = allContests.filter { it.isOutdatedFor(webUser) && webUser.startTimesByContestId.containsKey(it.id!!) }
//
//        model.addAttribute(OUTDATED_CONTESTS_ATTR, outdatedStudentContests.map { it.toStudentView(timezone) }.sortedBy { it.id })
//
//        return CONTESTS_PAGE
//    }
//
//    companion object {
//
//        const val CONTESTS_PATH = "$STUDENT_PATH/contests"
//        const val CONTESTS_PAGE = "$STUDENT_PAGE/contests"
//
//        const val CONTESTS_ATTR = "contests"
//        const val OUTDATED_CONTESTS_ATTR = "outdatedContests"
//    }
//}