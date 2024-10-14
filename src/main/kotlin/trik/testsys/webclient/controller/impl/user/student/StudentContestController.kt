package trik.testsys.webclient.controller.impl.user.student

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.impl.main.LoginController
import trik.testsys.webclient.controller.impl.user.student.StudentContestsController.Companion.CONTESTS_PATH
import trik.testsys.webclient.controller.impl.user.student.StudentMainController.Companion.STUDENT_PAGE
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.entity.impl.Solution
import trik.testsys.webclient.entity.user.impl.Student
import trik.testsys.webclient.service.FileManager
import trik.testsys.webclient.service.Grader
import trik.testsys.webclient.service.entity.impl.ContestService
import trik.testsys.webclient.service.entity.impl.SolutionService
import trik.testsys.webclient.service.entity.impl.TaskService
import trik.testsys.webclient.service.entity.user.impl.StudentService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.addPopupMessage
import trik.testsys.webclient.view.impl.StudentContestView.Companion.toStudentView
import trik.testsys.webclient.view.impl.StudentView
import trik.testsys.webclient.view.impl.TaskTestResultView.Companion.toTaskTestResultView
import trik.testsys.webclient.view.impl.TaskView.Companion.toView

@Controller
@RequestMapping(StudentContestController.CONTEST_PATH)
class StudentContestController(
    loginData: LoginData,

    private val studentService: StudentService,
    private val tasksService: TaskService,
    private val solutionService: SolutionService,
    private val contestService: ContestService,

    private val fileManager: FileManager,
    private val grader: Grader,

    @Value("\${trik.studio.version}") private val trikStudioVersion: String
) : AbstractWebUserController<Student, StudentView, StudentService>(loginData) {

    override val mainPage = CONTEST_PAGE

    override val mainPath = CONTEST_PATH

    override fun Student.toView(timeZoneId: String?) = TODO()

    @GetMapping("/start/{contestId}")
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
            redirectAttributes.addPopupMessage("Тур '${contest.name}' не доступен.")
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
        @RequestParam(name = "outdated", defaultValue = "false") isOutdated: Boolean,
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        if (isOutdated) {
            if (!webUser.startTimesByContestId.keys.contains(contestId)) {
                return "redirect:$CONTEST_PATH/start/$contestId"
            }

            val outdatedContest = contestService.find(contestId) ?: run {
                redirectAttributes.addPopupMessage("Тур c ID '$contestId' не найден.")
                return "redirect:$CONTESTS_PATH"
            }

            if (!outdatedContest.isOutdatedFor(webUser)) {
                redirectAttributes.addPopupMessage("Тур '${outdatedContest.name}' не закончен.")
                return "redirect:$CONTEST_PATH/$contestId"
            }

            model.addAttribute(CONTEST_ATTR, outdatedContest.toStudentView(timezone))
            val outdatedSolutions = solutionService.findByStudentAndContest(webUser, outdatedContest)
                .sortedByDescending { it.creationDate }
                .map { it.toTaskTestResultView(timezone) }
            model.addAttribute(SOLUTIONS_ATTR, outdatedSolutions)

            return CONTEST_PAGE
        }

        if (!webUser.group.isContestAvailable(contestId)) {
            redirectAttributes.addPopupMessage("Тур c ID '$contestId' не доступен.")
            return "redirect:$CONTESTS_PATH"
        }

        val contest = webUser.group.contests.first { it.id == contestId }
        if (!contest.isGoingOn()) {
            redirectAttributes.addPopupMessage("Тур '${contest.name}' не доступен.")
            return "redirect:$CONTESTS_PATH"
        }

        if (!webUser.startTimesByContestId.keys.contains(contest.id)) {
            return "redirect:$CONTEST_PATH/start/$contestId"
        }

        val lastTime = webUser.remainingTimeFor(contest)

        if (lastTime.toSecondOfDay() < 1) {
            redirectAttributes.addPopupMessage("Время на участие в Туре истекло.")
            return "redirect:$CONTESTS_PATH"
        }

        model.addAttribute(CONTEST_ATTR, contest.toStudentView(timezone, lastTime))

        val tasksView = contest.tasks
            .sortedBy { it.id }
            .map { it.toView(timezone) }
        model.addAttribute(TASKS_ATTR, tasksView)

        val solutions = solutionService.findByStudentAndContest(webUser, contest)
            .sortedByDescending { it.creationDate }
            .map { it.toTaskTestResultView(timezone) }
        model.addAttribute(SOLUTIONS_ATTR, solutions)

        return CONTEST_PAGE
    }

    @GetMapping("/downloadExercise/{contestId}")
    fun downloadExercise(
        @PathVariable contestId: Long,
        @RequestParam taskId: Long,
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): Any {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        if (!webUser.group.isContestAvailable(contestId)) {
            redirectAttributes.addPopupMessage("Тур c ID '$contestId' не доступен.")
            return "redirect:$CONTESTS_PATH"
        }

        val contest = webUser.group.contests.first { it.id == contestId }
        if (!contest.isGoingOn()) {
            redirectAttributes.addPopupMessage("Тур '${contest.name}' не доступен.")
            return "redirect:$CONTESTS_PATH"
        }

        val task = tasksService.find(taskId) ?: run {
            redirectAttributes.addPopupMessage("Задание c ID '$taskId' не найдено.")
            return "redirect:$CONTEST_PATH/$contestId"
        }

        if (contest.tasks.none { it.id == task.id }) {
            redirectAttributes.addPopupMessage("Задание c ID '$taskId' не доступно.")
            return "redirect:$CONTEST_PATH/$contestId"
        }

        if (!webUser.startTimesByContestId.keys.contains(contest.id)) {
            return "redirect:$CONTEST_PATH/start/$contestId"
        }

        if (webUser.remainingTimeFor(contest).toSecondOfDay() < 1) {
            redirectAttributes.addPopupMessage("Время на решение Задания истекло.")
            return "redirect:$CONTEST_PATH/$contestId"
        }

        val exercise = fileManager.getTaskFile(task.exercise!!)!!

        val responseEntity = ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=${task.id}.qrs")
            .body(exercise.readBytes())

        return responseEntity
    }

    @GetMapping("/downloadCondition/{contestId}")
    fun downloadCondition(
        @PathVariable contestId: Long,
        @RequestParam taskId: Long,
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): Any {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        if (!webUser.group.isContestAvailable(contestId)) {
            redirectAttributes.addPopupMessage("Тур c ID '$contestId' не доступен.")
            return "redirect:$CONTESTS_PATH"
        }

        val contest = webUser.group.contests.first { it.id == contestId }
        if (!contest.isGoingOn()) {
            redirectAttributes.addPopupMessage("Тур '${contest.name}' не доступен.")
            return "redirect:$CONTESTS_PATH"
        }

        val task = tasksService.find(taskId) ?: run {
            redirectAttributes.addPopupMessage("Задание c ID '$taskId' не найдено.")
            return "redirect:$CONTEST_PATH/$contestId"
        }

        if (contest.tasks.none { it.id == task.id }) {
            redirectAttributes.addPopupMessage("Задание c ID '$taskId' не доступно.")
            return "redirect:$CONTEST_PATH/$contestId"
        }

        if (!webUser.startTimesByContestId.keys.contains(contest.id)) {
            return "redirect:$CONTEST_PATH/start/$contestId"
        }

        if (webUser.remainingTimeFor(contest).toSecondOfDay() < 1) {
            redirectAttributes.addPopupMessage("Время на решение Задания истекло.")
            return "redirect:$CONTEST_PATH/$contestId"
        }

        if (!task.hasCondition) {
            redirectAttributes.addPopupMessage("У задания '${task.name}' нет Условия.")
            return "redirect:$CONTEST_PATH/$contestId"
        }

        val condition = fileManager.getTaskFile(task.condition!!)!!

        val responseEntity = ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=${task.id}.pdf")
            .body(condition.readBytes())

        return responseEntity
    }


    @PostMapping("/submitSolution/{contestId}")
    fun submitSolution(
        @PathVariable contestId: Long,
        @RequestParam taskId: Long,
        @RequestParam("file") file: MultipartFile,
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
            redirectAttributes.addPopupMessage("Тур '${contest.name}' не доступен.")
            return "redirect:$CONTESTS_PATH"
        }

        val task = tasksService.find(taskId) ?: run {
            redirectAttributes.addPopupMessage("Задание c ID '$taskId' не найдено.")
            return "redirect:$CONTEST_PATH/$contestId"
        }

        if (contest.tasks.none { it.id == task.id }) {
            redirectAttributes.addPopupMessage("Задание c ID '$taskId' не доступно.")
            return "redirect:$CONTEST_PATH/$contestId"
        }

        if (!webUser.startTimesByContestId.keys.contains(contest.id)) {
            return "redirect:$CONTEST_PATH/start/$contestId"
        }

        if (webUser.remainingTimeFor(contest).toSecondOfDay() < 1) {
            redirectAttributes.addPopupMessage("Время на решение Задания истекло.")
            return "redirect:$CONTEST_PATH/$contestId"
        }

        val solution = Solution.qrsSolution(task).also {
            it.student = webUser
        }
        solutionService.save(solution)

        fileManager.saveSolutionFile(solution, file)
        grader.sendToGrade(
            solution,
            Grader.GradingOptions(true, trikStudioVersion)
        )

        redirectAttributes.addPopupMessage("Решение отправлено.")

        return "redirect:$CONTEST_PATH/$contestId"
    }

    companion object {

        const val CONTEST_PATH = "$CONTESTS_PATH/contest"
        const val CONTEST_PAGE = "$STUDENT_PAGE/contest"

        const val CONTEST_ATTR = "contest"
        const val TASKS_ATTR = "tasks"
        const val SOLUTIONS_ATTR = "solutions"

        fun Group.isContestAvailable(contestId: Long) = contests.any { it.id == contestId }
    }
}