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
import trik.testsys.webapp.backoffice.utils.addMessage
import java.time.Duration
import java.time.Instant

@Controller
@RequestMapping("/user/student/contests/{id}")
class StudentContestController(
    private val contestService: ContestService,
    private val verdictService: VerdictService,
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
            startContestIfNeeded(session, contest)
        }

        setupModel(model, session, user)
        model.addAttribute("contest", contest)
        val tasks = contest.tasks.sortedBy { it.id }
        model.addAttribute("tasks", tasks)
        model.addAttribute("remainingTime", remainingTimeString(session, Instant.now(), contest))

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

    private fun startContestIfNeeded(session: HttpSession, contest: Contest) {
        @Suppress("UNCHECKED_CAST")
        var map = session.getAttribute(SESSION_START_TIMES) as? MutableMap<Long, Instant>
        if (map == null) {
            map = LinkedHashMap()
            session.setAttribute(SESSION_START_TIMES, map)
        }
        map.putIfAbsent(contest.id!!, Instant.now())
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
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.http.MediaType
//import org.springframework.http.ResponseEntity
//import org.springframework.stereotype.Controller
//import org.springframework.ui.Model
//import org.springframework.web.bind.annotation.*
//import org.springframework.web.multipart.MultipartFile
//import org.springframework.web.servlet.mvc.support.RedirectAttributes
//import trik.testsys.backoffice.controller.impl.main.LoginController
//import trik.testsys.backoffice.controller.impl.user.student.StudentContestsController.Companion.CONTESTS_PATH
//import trik.testsys.backoffice.controller.impl.user.student.StudentMainController.Companion.STUDENT_PAGE
//import trik.testsys.backoffice.controller.user.AbstractWebUserController
//import trik.testsys.backoffice.entity.impl.StudentGroup
//import trik.testsys.backoffice.entity.impl.Solution
//import trik.testsys.backoffice.entity.user.impl.Student
//import trik.testsys.backoffice.service.FileManager
//import trik.testsys.backoffice.service.Grader
//import trik.testsys.backoffice.service.entity.impl.ContestService
//import trik.testsys.backoffice.service.entity.impl.SolutionService
//import trik.testsys.backoffice.service.entity.impl.TaskService
//import trik.testsys.backoffice.service.entity.user.impl.StudentService
//import trik.testsys.backoffice.service.security.login.impl.LoginData
//import trik.testsys.backoffice.util.addPopupMessage
//import trik.testsys.backoffice.view.impl.StudentContestView.Companion.toStudentView
//import trik.testsys.backoffice.view.impl.StudentView
//import trik.testsys.backoffice.view.impl.TaskTestResultView.Companion.toTaskTestResultView
//
//@Controller
//@RequestMapping(StudentContestController.CONTEST_PATH)
//class StudentContestController(
//    loginData: LoginData,
//
//    private val studentService: StudentService,
//    private val tasksService: TaskService,
//    private val solutionService: SolutionService,
//    private val contestService: ContestService,
//
//    private val fileManager: FileManager,
//    private val grader: Grader,
//
//    @Value("\${trik-studio-version}") private val trikStudioVersion: String
//) : AbstractWebUserController<Student, StudentView, StudentService>(loginData) {
//
//    override val mainPage = CONTEST_PAGE
//
//    override val mainPath = CONTEST_PATH
//
//    override fun Student.toView(timeZoneId: String?) = TODO()
//
//    @GetMapping("/start/{contestId}")
//    fun contestStart(
//        @PathVariable contestId: Long,
//        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
//        redirectAttributes: RedirectAttributes,
//        model: Model
//    ): String {
//        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"
//
//        if (!webUser.group.isContestAvailable(contestId)) {
//            redirectAttributes.addPopupMessage("Тур c ID '$contestId' не доступен.")
//            return "redirect:$CONTESTS_PATH"
//        }
//
//        val contest = webUser.group.contests.first { it.id == contestId }
//        if (!contest.isGoingOn()) {
//            redirectAttributes.addPopupMessage("Тур '${contest.name}' не доступен.")
//            return "redirect:$CONTESTS_PATH"
//        }
//
//        if (webUser.startTimesByContestId.keys.contains(contest.id)) {
//            return "redirect:$CONTEST_PATH/$contestId"
//        }
//
//        webUser.startContest(contest)
//        studentService.save(webUser)
//
//        redirectAttributes.addPopupMessage("Тур '${contest.name}' начат.")
//
//        return "redirect:$CONTEST_PATH/$contestId"
//    }
//
//    @GetMapping("/{contestId}")
//    fun contestGet(
//        @PathVariable contestId: Long,
//        @RequestParam(name = "outdated", defaultValue = "false") isOutdated: Boolean,
//        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
//        redirectAttributes: RedirectAttributes,
//        model: Model
//    ): String {
//        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"
//
//        if (isOutdated) {
//            if (!webUser.startTimesByContestId.keys.contains(contestId)) {
//                return "redirect:$CONTEST_PATH/start/$contestId"
//            }
//
//            val outdatedContest = contestService.find(contestId) ?: run {
//                redirectAttributes.addPopupMessage("Тур c ID '$contestId' не найден.")
//                return "redirect:$CONTESTS_PATH"
//            }
//
//            if (!outdatedContest.isOutdatedFor(webUser)) {
//                redirectAttributes.addPopupMessage("Тур '${outdatedContest.name}' не закончен.")
//                return "redirect:$CONTEST_PATH/$contestId"
//            }
//
//            model.addAttribute(CONTEST_ATTR, outdatedContest.toStudentView(timezone))
//            val outdatedSolutions = solutionService.findByStudentAndContest(webUser, outdatedContest)
//                .sortedByDescending { it.creationDate }
//                .map { it.toTaskTestResultView(timezone) }
//            model.addAttribute(SOLUTIONS_ATTR, outdatedSolutions)
//
//            return CONTEST_PAGE
//        }
//
//        if (!webUser.group.isContestAvailable(contestId)) {
//            redirectAttributes.addPopupMessage("Тур c ID '$contestId' не доступен.")
//            return "redirect:$CONTESTS_PATH"
//        }
//
//        val contest = webUser.group.contests.first { it.id == contestId }
//        if (!contest.isGoingOn()) {
//            redirectAttributes.addPopupMessage("Тур '${contest.name}' не доступен.")
//            return "redirect:$CONTESTS_PATH"
//        }
//
//        if (!webUser.startTimesByContestId.keys.contains(contest.id)) {
//            return "redirect:$CONTEST_PATH/start/$contestId"
//        }
//
//        val lastTime = webUser.remainingTimeFor(contest)
//
//        if (lastTime.toSecondOfDay() < 1) {
//            redirectAttributes.addPopupMessage("Время на участие в Туре истекло.")
//            return "redirect:$CONTESTS_PATH"
//        }
//
//        model.addAttribute(CONTEST_ATTR, contest.toStudentView(timezone, lastTime))
//
//        val tasksView = contest.tasks
//            .sortedBy { it.id }
//            .map { it.toView(timezone) }
//        model.addAttribute(TASKS_ATTR, tasksView)
//
//        val solutions = solutionService.findByStudentAndContest(webUser, contest)
//            .sortedByDescending { it.creationDate }
//            .map { it.toTaskTestResultView(timezone) }
//        model.addAttribute(SOLUTIONS_ATTR, solutions)
//
//        return CONTEST_PAGE
//    }
//
//    @GetMapping("/downloadResults/{contestId}")
//    fun downloadTaskResults(
//        @PathVariable contestId: Long,
//        @RequestParam solutionId: Long,
//        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
//        redirectAttributes: RedirectAttributes,
//        model: Model
//    ): Any {
//        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"
//
//        val contest = contestService.find(contestId) ?: run {
//            redirectAttributes.addPopupMessage("Тур c ID '$contestId' не найден.")
//            return "redirect:$CONTESTS_PATH"
//        }
//
//        val solution = solutionService.find(solutionId) ?: run {
//            redirectAttributes.addPopupMessage("Решение c ID '$solutionId' не найдено.")
//            return "redirect:$CONTEST_PATH/$contestId"
//        }
//
//        if (solution.student?.id != webUser.id) {
//            redirectAttributes.addPopupMessage("Решение c ID '$solutionId' не найдено.")
//            return "redirect:$CONTEST_PATH/$contestId"
//        }
//
//        val task = solution.task
//
//        if (contest.tasks.none { it.id == task.id }) {
//            redirectAttributes.addPopupMessage("Решение c ID '$solutionId' не найдено.")
//            return "redirect:$CONTEST_PATH/$contestId"
//        }
//
//        val results = fileManager.getSolutionResultFilesCompressed(solution)
//        val bytes = results.readBytes()
//
//        val responseEntity = ResponseEntity.ok()
//            .header("Content-Disposition", "attachment; filename=\"${results.name}\"")
//            .header("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE)
//            .header("Content-Transfer-Encoding", "binary")
//            .header("Content-Length", bytes.size.toString())
//            .body(bytes)
//
//        return responseEntity
//    }
//
//    @GetMapping("/downloadExercise/{contestId}")
//    fun downloadExercise(
//        @PathVariable contestId: Long,
//        @RequestParam taskId: Long,
//        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
//        redirectAttributes: RedirectAttributes,
//        model: Model
//    ): Any {
//        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"
//
//        if (!webUser.group.isContestAvailable(contestId)) {
//            redirectAttributes.addPopupMessage("Тур c ID '$contestId' не доступен.")
//            return "redirect:$CONTESTS_PATH"
//        }
//
//        val contest = webUser.group.contests.first { it.id == contestId }
//        if (!contest.isGoingOn()) {
//            redirectAttributes.addPopupMessage("Тур '${contest.name}' не доступен.")
//            return "redirect:$CONTESTS_PATH"
//        }
//
//        val task = tasksService.find(taskId) ?: run {
//            redirectAttributes.addPopupMessage("Задание c ID '$taskId' не найдено.")
//            return "redirect:$CONTEST_PATH/$contestId"
//        }
//
//        if (contest.tasks.none { it.id == task.id }) {
//            redirectAttributes.addPopupMessage("Задание c ID '$taskId' не доступно.")
//            return "redirect:$CONTEST_PATH/$contestId"
//        }
//
//        if (!webUser.startTimesByContestId.keys.contains(contest.id)) {
//            return "redirect:$CONTEST_PATH/start/$contestId"
//        }
//
//        if (webUser.remainingTimeFor(contest).toSecondOfDay() < 1) {
//            redirectAttributes.addPopupMessage("Время на решение Задания истекло.")
//            return "redirect:$CONTEST_PATH/$contestId"
//        }
//
//        val exercise = fileManager.getTaskFile(task.exercise!!)!!
//
//        val responseEntity = ResponseEntity.ok()
//            .header("Content-Disposition", "attachment; filename=${task.id}.qrs")
//            .body(exercise.readBytes())
//
//        return responseEntity
//    }
//
//    @GetMapping("/downloadCondition/{contestId}")
//    fun downloadCondition(
//        @PathVariable contestId: Long,
//        @RequestParam taskId: Long,
//        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
//        redirectAttributes: RedirectAttributes,
//        model: Model
//    ): Any {
//        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"
//
//        if (!webUser.group.isContestAvailable(contestId)) {
//            redirectAttributes.addPopupMessage("Тур c ID '$contestId' не доступен.")
//            return "redirect:$CONTESTS_PATH"
//        }
//
//        val contest = webUser.group.contests.first { it.id == contestId }
//        if (!contest.isGoingOn()) {
//            redirectAttributes.addPopupMessage("Тур '${contest.name}' не доступен.")
//            return "redirect:$CONTESTS_PATH"
//        }
//
//        val task = tasksService.find(taskId) ?: run {
//            redirectAttributes.addPopupMessage("Задание c ID '$taskId' не найдено.")
//            return "redirect:$CONTEST_PATH/$contestId"
//        }
//
//        if (contest.tasks.none { it.id == task.id }) {
//            redirectAttributes.addPopupMessage("Задание c ID '$taskId' не доступно.")
//            return "redirect:$CONTEST_PATH/$contestId"
//        }
//
//        if (!webUser.startTimesByContestId.keys.contains(contest.id)) {
//            return "redirect:$CONTEST_PATH/start/$contestId"
//        }
//
//        if (webUser.remainingTimeFor(contest).toSecondOfDay() < 1) {
//            redirectAttributes.addPopupMessage("Время на решение Задания истекло.")
//            return "redirect:$CONTEST_PATH/$contestId"
//        }
//
//        if (!task.hasCondition) {
//            redirectAttributes.addPopupMessage("У задания '${task.name}' нет Условия.")
//            return "redirect:$CONTEST_PATH/$contestId"
//        }
//
//        val condition = fileManager.getTaskFile(task.condition!!)!!
//
//        val responseEntity = ResponseEntity.ok()
//            .header("Content-Disposition", "attachment; filename=${task.id}.pdf")
//            .body(condition.readBytes())
//
//        return responseEntity
//    }
//
//
//    @PostMapping("/submitSolution/{contestId}")
//    fun submitSolution(
//        @PathVariable contestId: Long,
//        @RequestParam taskId: Long,
//        @RequestParam("file") file: MultipartFile,
//        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
//        redirectAttributes: RedirectAttributes,
//        model: Model
//    ): String {
//        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"
//
//        if (!webUser.group.isContestAvailable(contestId)) {
//            redirectAttributes.addPopupMessage("Тур c ID '$contestId' не доступен.")
//            return "redirect:$CONTESTS_PATH"
//        }
//
//        val contest = webUser.group.contests.first { it.id == contestId }
//        if (!contest.isGoingOn()) {
//            redirectAttributes.addPopupMessage("Тур '${contest.name}' не доступен.")
//            return "redirect:$CONTESTS_PATH"
//        }
//
//        val task = tasksService.find(taskId) ?: run {
//            redirectAttributes.addPopupMessage("Задание c ID '$taskId' не найдено.")
//            return "redirect:$CONTEST_PATH/$contestId"
//        }
//
//        if (contest.tasks.none { it.id == task.id }) {
//            redirectAttributes.addPopupMessage("Задание c ID '$taskId' не доступно.")
//            return "redirect:$CONTEST_PATH/$contestId"
//        }
//
//        if (!webUser.startTimesByContestId.keys.contains(contest.id)) {
//            return "redirect:$CONTEST_PATH/start/$contestId"
//        }
//
//        if (webUser.remainingTimeFor(contest).toSecondOfDay() < 1) {
//            redirectAttributes.addPopupMessage("Время на решение Задания истекло.")
//            return "redirect:$CONTEST_PATH/$contestId"
//        }
//
//        val solution = Solution.qrsSolution(task).also {
//            it.student = webUser
//            it.additionalInfo = "Оригинальное решение Участника '${webUser.id}: ${webUser.name}' для Задания '${task.id}: ${task.name}'."
//        }
//        solutionService.save(solution)
//
//        fileManager.saveSolutionFile(solution, file)
//        grader.sendToGrade(
//            solution,
//            Grader.GradingOptions(true, trikStudioVersion)
//        )
//
//        redirectAttributes.addPopupMessage("Решение отправлено.")
//
//        return "redirect:$CONTEST_PATH/$contestId"
//    }
//
//    companion object {
//
//        const val CONTEST_PATH = "$CONTESTS_PATH/contest"
//        const val CONTEST_PAGE = "$STUDENT_PAGE/contest"
//
//        const val CONTEST_ATTR = "contest"
//        const val TASKS_ATTR = "tasks"
//        const val SOLUTIONS_ATTR = "solutions"
//
//        fun StudentGroup.isContestAvailable(contestId: Long) = contests.any { it.id == contestId }
//    }
//}