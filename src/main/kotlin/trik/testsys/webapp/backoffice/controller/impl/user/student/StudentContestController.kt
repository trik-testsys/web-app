package trik.testsys.webapp.backoffice.controller.impl.user.student

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webapp.backoffice.controller.AbstractUserController
import trik.testsys.webapp.backoffice.data.entity.impl.Contest
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.service.ContestService
import trik.testsys.webapp.backoffice.data.service.VerdictService
import trik.testsys.webapp.backoffice.data.service.ContestRunService
import trik.testsys.webapp.backoffice.utils.addMessage
import java.time.Duration
import java.time.Instant

@Controller
@RequestMapping("/user/student/contests/{id}")
class StudentContestController(
    private val contestService: ContestService,
    private val verdictService: VerdictService,
    private val contestRunService: ContestRunService,
) : AbstractUserController() {

    @GetMapping
    fun view(
        @PathVariable("id") id: Long,
        @RequestParam(name = "start", required = false, defaultValue = "false") start: Boolean,
        model: Model,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val user = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!user.privileges.contains(User.Privilege.STUDENT)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val contest = contestService.findById(id) ?: run {
            redirectAttributes.addMessage("Тур не найден.")
            return "redirect:/user/student/contests"
        }

        // Verify accessibility via student's groups
        val inStudentGroups = user.memberedStudentGroups.any { g -> g.contests.any { it.id == contest.id } }
        if (!inStudentGroups) {
            redirectAttributes.addMessage("Тур недоступен.")
            return "redirect:/user/student/contests"
        }

        // Start on demand (after user confirmation on UI)
        if (start) {
            startContestIfNeeded(user, contest)
        }

        setupModel(model, session, user)
        model.addAttribute("contest", contest)
        val tasks = contest.tasks.sortedBy { it.id }
        model.addAttribute("tasks", tasks)
        model.addAttribute("remainingTime", remainingTimeString(user, Instant.now(), contest))

        val userSolutions = contest.solutions.filter { it.createdBy.id == user.id }
        val verdicts = verdictService.findAllBySolutions(userSolutions)

        val taskScores = mutableMapOf<Long, Long>()
        userSolutions.forEach { sol ->
            val solVerdicts = verdicts.filter { it.solutionId == sol.id }
            val maxScore = solVerdicts.maxOfOrNull { it.value }
            if (maxScore != null) {
                val taskId = sol.task.id!!
                val current = taskScores[taskId]
                if (current == null || maxScore > current) {
                    taskScores[taskId] = maxScore
                }
            }
        }
        model.addAttribute("taskScores", taskScores)
        return "student/contest"
    }

    private fun startContestIfNeeded(user: User, contest: Contest) {
        contestRunService.startIfAbsent(user, contest)
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

    
}