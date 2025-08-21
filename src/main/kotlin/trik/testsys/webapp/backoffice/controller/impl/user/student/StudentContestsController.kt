package trik.testsys.webapp.backoffice.controller.impl.user.student

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webapp.backoffice.controller.AbstractUserController
import trik.testsys.webapp.backoffice.data.entity.impl.Contest
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.service.ContestService
import trik.testsys.webapp.backoffice.utils.addMessage
import java.time.Instant
import java.time.Duration

@Controller
@RequestMapping("/user/student/contests")
class StudentContestsController(
    private val contestService: ContestService,
) : AbstractUserController() {

    @GetMapping
    fun page(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val user = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!user.privileges.contains(User.Privilege.STUDENT)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        // Available/Ended split with personal duration respected
        val now = Instant.now()
        val studentGroupContests = user.memberedStudentGroups.flatMap { it.contests }.toSet()

        val available = studentGroupContests
            .filter { c ->
                val starts = c.startsAt
                val ends = c.endsAt
                val windowOpen = when {
                    starts == null -> true
                    ends == null -> now.isAfter(starts)
                    else -> now.isAfter(starts) && now.isBefore(ends)
                }
                if (!windowOpen) return@filter false

                val started = getContestStartTime(session, c.id!!) != null
                if (!started) return@filter true

                val rd = remainingDuration(session, now, c)
                rd == null || rd.seconds > 0
            }
            .sortedBy { it.id }

        val ended = studentGroupContests
            .filter { c ->
                val started = getContestStartTime(session, c.id!!) != null
                if (!started) return@filter false
                val ends = c.endsAt
                val endedByDate = ends != null && now.isAfter(ends)
                val rd = remainingDuration(session, now, c)
                val endedByDuration = rd != null && (rd.isZero || rd.isNegative)
                endedByDate || endedByDuration
            }
            .sortedBy { it.id }

        setupModel(model, session, user)
        model.addAttribute("availableContests", available)
        model.addAttribute("endedContests", ended)
        // Remaining time per contest using session-based start moment
        val remainingByContestId = (available + ended).associate { it.id!! to remainingTimeString(session, now, it) }
        model.addAttribute("remainingByContestId", remainingByContestId)
        val startedByContestId = (available + ended).associate { it.id!! to (getContestStartTime(session, it.id!!) != null) }
        model.addAttribute("startedByContestId", startedByContestId)
        return "student/contests"
    }

    private fun remainingTimeString(session: HttpSession, now: Instant, contest: Contest): String {
        val durationMinutes = contest.duration
        val endsAt = contest.endsAt

        if (endsAt == null && durationMinutes == null) return "—"

        val startInstant = getContestStartTime(session, contest.id!!)

        var timeByEnds: Duration? = null
        if (endsAt != null) {
            timeByEnds = Duration.between(now, endsAt)
        }

        var timeByDuration: Duration? = null
        if (durationMinutes != null && startInstant != null) {
            val elapsed = Duration.between(startInstant, now)
            val total = Duration.ofMinutes(durationMinutes)
            timeByDuration = total.minus(elapsed)
        }

        val effective = when {
            endsAt == null -> timeByDuration
            durationMinutes == null -> timeByEnds
            else -> listOfNotNull(timeByEnds, timeByDuration).minByOrNull { it }
        }

        if (effective == null) return "—"
        if (effective.isNegative || effective.isZero) return "00:00:00"
        val totalSeconds = effective.seconds
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun remainingDuration(session: HttpSession, now: Instant, contest: Contest): Duration? {
        val durationMinutes = contest.duration
        val endsAt = contest.endsAt
        if (endsAt == null && durationMinutes == null) return null
        val startInstant = getContestStartTime(session, contest.id!!)
        var timeByEnds: Duration? = null
        if (endsAt != null) timeByEnds = Duration.between(now, endsAt)
        var timeByDuration: Duration? = null
        if (durationMinutes != null && startInstant != null) {
            val elapsed = Duration.between(startInstant, now)
            val total = Duration.ofMinutes(durationMinutes)
            timeByDuration = total.minus(elapsed)
        }
        return when {
            endsAt == null -> timeByDuration
            durationMinutes == null -> timeByEnds
            else -> listOfNotNull(timeByEnds, timeByDuration).minByOrNull { it }
        }
    }

    private fun getContestStartTime(session: HttpSession, contestId: Long): Instant? {
        @Suppress("UNCHECKED_CAST")
        val map = session.getAttribute(SESSION_START_TIMES) as? MutableMap<Long, Instant>
        return map?.get(contestId)
    }

    companion object {
        private const val SESSION_START_TIMES = "studentContestStartTimes"
    }
}

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