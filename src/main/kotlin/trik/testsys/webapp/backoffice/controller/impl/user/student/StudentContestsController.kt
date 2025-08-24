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
import trik.testsys.webapp.backoffice.data.service.ContestRunService
import trik.testsys.webapp.backoffice.utils.addMessage
import java.time.Instant
import java.time.Duration

@Controller
@RequestMapping("/user/student/contests")
class StudentContestsController(
    private val contestService: ContestService,
    private val contestRunService: ContestRunService,
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

        val upcoming = studentGroupContests
            .filter { c ->
                val starts = c.startsAt
                starts != null && now.isBefore(starts)
            }
            .sortedBy { it.id }

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

                val started = contestRunService.findByUserAndContest(user, c) != null
                if (!started) return@filter true

                val rd = remainingDuration(user, now, c)
                rd == null || rd.seconds > 0
            }
            .sortedBy { it.id }

        val ended = studentGroupContests
            .filter { c ->
                val started = contestRunService.findByUserAndContest(user, c) != null
                if (!started) return@filter false
                val ends = c.endsAt
                val endedByDate = ends != null && now.isAfter(ends)
                val rd = remainingDuration(user, now, c)
                val endedByDuration = rd != null && (rd.isZero || rd.isNegative)
                endedByDate || endedByDuration
            }
            .sortedBy { it.id }

        setupModel(model, session, user)
        model.addAttribute("upcomingContests", upcoming)
        model.addAttribute("availableContests", available)
        model.addAttribute("endedContests", ended)
        // Remaining time per contest using session-based start moment
        val remainingByContestId = (available + ended).associate { it.id!! to remainingTimeString(user, now, it) }
        model.addAttribute("remainingByContestId", remainingByContestId)
        val startedByContestId = (available + ended).associate { it.id!! to (contestRunService.findByUserAndContest(user, it) != null) }
        model.addAttribute("startedByContestId", startedByContestId)
        return "student/contests"
    }

    private fun remainingTimeString(user: User, now: Instant, contest: Contest): String {
        val durationMinutes = contest.duration
        val endsAt = contest.endsAt

        if (endsAt == null && durationMinutes == null) return "—"

        val startInstant = contestRunService.findByUserAndContest(user, contest)?.startedAt

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

    private fun remainingDuration(user: User, now: Instant, contest: Contest): Duration? {
        val durationMinutes = contest.duration
        val endsAt = contest.endsAt
        if (endsAt == null && durationMinutes == null) return null
        val startInstant = contestRunService.findByUserAndContest(user, contest)?.startedAt
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

}
