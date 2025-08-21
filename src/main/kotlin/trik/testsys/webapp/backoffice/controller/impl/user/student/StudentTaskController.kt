package trik.testsys.webapp.backoffice.controller.impl.user.student

import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import trik.testsys.webapp.backoffice.controller.AbstractUserController
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.service.ContestService
import trik.testsys.webapp.backoffice.data.service.SolutionService
import trik.testsys.webapp.backoffice.data.service.TaskService
import trik.testsys.webapp.backoffice.service.FileManager
import trik.testsys.webapp.backoffice.service.Grader
import trik.testsys.webapp.backoffice.utils.addMessage
import org.springframework.web.multipart.MultipartFile
import trik.testsys.webapp.backoffice.data.service.VerdictService

@Controller
@RequestMapping("/user/student/contests/{contestId}/tasks/{taskId}")
class StudentTaskController(
    private val contestService: ContestService,
    private val taskService: TaskService,
    private val solutionService: SolutionService,
    private val fileManager: FileManager,
    private val grader: Grader,
    private val verdictService: VerdictService,

    @Value("\${trik.testsys.trik-studio.container.name}")
    private val trikStudioContainerName: String,
) : AbstractUserController() {

    @GetMapping
    fun view(
        @PathVariable("contestId") contestId: Long,
        @PathVariable("taskId") taskId: Long,
        model: Model,
        session: HttpSession,
        redirectAttributes: RedirectAttributes,
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val user = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!user.privileges.contains(User.Privilege.STUDENT)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val contest = contestService.findById(contestId) ?: run {
            redirectAttributes.addMessage("Тур не найден.")
            return "redirect:/user/student/contests"
        }
        val task = taskService.findById(taskId) ?: run {
            redirectAttributes.addMessage("Задача не найдена.")
            return "redirect:/user/student/contests/$contestId"
        }

        // Ensure task belongs to contest
        if (contest.tasks.none { it.id == task.id }) {
            redirectAttributes.addMessage("Задача не принадлежит данному Туру.")
            return "redirect:/user/student/contests/$contestId"
        }

        setupModel(model, session, user)
        model.addAttribute("contest", contest)
        model.addAttribute("task", task)
        val solutions = contest.solutions.filter { it.task.id == task.id }
            .filter { it.createdBy.id == user.id }
            .sortedByDescending { it.id }
        val verdicts = verdictService.findAllBySolutions(solutions)
        val verdictsBySolutionId = verdicts.associateBy { it.solutionId }

        model.addAttribute("solutions", solutions)
        model.addAttribute("verdicts", verdictsBySolutionId)
        val resultsAvailability = solutions.associate { s ->
            val hasVerdicts = fileManager.getVerdictFiles(s).isNotEmpty()
            val hasRecordings = fileManager.getRecordingFiles(s).isNotEmpty()
            (s.id!!) to (hasVerdicts || hasRecordings)
        }
        model.addAttribute("resultsAvailable", resultsAvailability)
        val maxScore = verdicts.maxOfOrNull { it.value } ?: 0
        model.addAttribute("maxScore", maxScore)
        val hasExercise = task.taskFiles.any { it.type == trik.testsys.webapp.backoffice.data.entity.impl.TaskFile.TaskFileType.EXERCISE }
        val hasCondition = task.taskFiles.any { it.type == trik.testsys.webapp.backoffice.data.entity.impl.TaskFile.TaskFileType.CONDITION }
        model.addAttribute("hasExercise", hasExercise)
        model.addAttribute("hasCondition", hasCondition)
        return "student/task"
    }

    @PostMapping
    fun upload(
        @PathVariable("contestId") contestId: Long,
        @PathVariable("taskId") taskId: Long,
        @RequestParam("file") file: MultipartFile,
        session: HttpSession,
        redirectAttributes: RedirectAttributes,
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val user = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!user.privileges.contains(User.Privilege.STUDENT)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val contest = contestService.findById(contestId) ?: run {
            redirectAttributes.addMessage("Тур не найден.")
            return "redirect:/user/student/contests"
        }
        val task = taskService.findById(taskId) ?: run {
            redirectAttributes.addMessage("Задача не найдена.")
            return "redirect:/user/student/contests/$contestId"
        }
        if (contest.tasks.none { it.id == task.id }) {
            redirectAttributes.addMessage("Задача не принадлежит данному Туру.")
            return "redirect:/user/student/contests/$contestId"
        }

        val solution = Solution().also {
            it.createdBy = user
            it.contest = contest
            it.task = task
        }
        val saved = solutionService.save(solution)
        val ok = fileManager.saveSolutionFile(saved, file)
        if (!ok) {
            redirectAttributes.addMessage("Не удалось сохранить файл решения.")
            return "redirect:/user/student/contests/$contestId/tasks/$taskId"
        }

        grader.sendToGrade(saved, Grader.GradingOptions(shouldRecordRun = true, trikStudioVersion = trikStudioContainerName))
        redirectAttributes.addMessage("Решение загружено и отправлено на проверку.")
        return "redirect:/user/student/contests/$contestId/tasks/$taskId"
    }

    @GetMapping("/results/{solutionId}/download")
    fun downloadResults(
        @PathVariable("contestId") contestId: Long,
        @PathVariable("taskId") taskId: Long,
        @PathVariable("solutionId") solutionId: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes,
    ): Any {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val user = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!user.privileges.contains(User.Privilege.STUDENT)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val contest = contestService.findById(contestId) ?: run {
            redirectAttributes.addMessage("Тур не найден.")
            return "redirect:/user/student/contests"
        }
        val task = taskService.findById(taskId) ?: run {
            redirectAttributes.addMessage("Задача не найдена.")
            return "redirect:/user/student/contests/$contestId"
        }
        if (contest.tasks.none { it.id == task.id }) {
            redirectAttributes.addMessage("Задача не принадлежит данному Туру.")
            return "redirect:/user/student/contests/$contestId"
        }

        val solution = contest.solutions.firstOrNull { it.id == solutionId } ?: run {
            redirectAttributes.addMessage("Решение не найдено.")
            return "redirect:/user/student/contests/$contestId/tasks/$taskId"
        }
        if (solution.createdBy.id != user.id) {
            redirectAttributes.addMessage("У вас нет доступа к данному Решению.")
            return "redirect:/user/student/contests/$contestId/tasks/$taskId"
        }

        val hasAnyResults = fileManager.getVerdictFiles(solution).isNotEmpty() || fileManager.getRecordingFiles(solution).isNotEmpty()
        if (!hasAnyResults) {
            redirectAttributes.addMessage("Результаты для данного Решения отсутствуют.")
            return "redirect:/user/student/contests/$contestId/tasks/$taskId"
        }

        val results = fileManager.getSolutionResultFilesCompressed(solution)
        val bytes = results.readBytes()
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"${results.name}\"")
            .header("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .header("Content-Transfer-Encoding", "binary")
            .header("Content-Length", bytes.size.toString())
            .body(bytes)
    }

    @GetMapping("/download/exercise")
    fun downloadExercise(
        @PathVariable("contestId") contestId: Long,
        @PathVariable("taskId") taskId: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes,
    ): Any {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val user = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!user.privileges.contains(User.Privilege.STUDENT)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val contest = contestService.findById(contestId) ?: run {
            redirectAttributes.addMessage("Тур не найден.")
            return "redirect:/user/student/contests"
        }
        val task = taskService.findById(taskId) ?: run {
            redirectAttributes.addMessage("Задача не найдена.")
            return "redirect:/user/student/contests/$contestId"
        }
        if (contest.tasks.none { it.id == task.id }) {
            redirectAttributes.addMessage("Задача не принадлежит данному Туру.")
            return "redirect:/user/student/contests/$contestId"
        }

        val exerciseTf = task.taskFiles.firstOrNull { it.type == trik.testsys.webapp.backoffice.data.entity.impl.TaskFile.TaskFileType.EXERCISE } ?: run {
            redirectAttributes.addMessage("Упражнение не найдено.")
            return "redirect:/user/student/contests/$contestId/tasks/$taskId"
        }
        val file = fileManager.getTaskFile(exerciseTf) ?: run {
            redirectAttributes.addMessage("Файл упражнения отсутствует на сервере.")
            return "redirect:/user/student/contests/$contestId/tasks/$taskId"
        }

        val bytes = file.readBytes()
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"${task.id}.qrs\"")
            .header("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .header("Content-Length", bytes.size.toString())
            .body(bytes)
    }

    @GetMapping("/download/condition")
    fun downloadCondition(
        @PathVariable("contestId") contestId: Long,
        @PathVariable("taskId") taskId: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes,
    ): Any {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val user = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!user.privileges.contains(User.Privilege.STUDENT)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val contest = contestService.findById(contestId) ?: run {
            redirectAttributes.addMessage("Тур не найден.")
            return "redirect:/user/student/contests"
        }
        val task = taskService.findById(taskId) ?: run {
            redirectAttributes.addMessage("Задача не найдена.")
            return "redirect:/user/student/contests/$contestId"
        }
        if (contest.tasks.none { it.id == task.id }) {
            redirectAttributes.addMessage("Задача не принадлежит данному Туру.")
            return "redirect:/user/student/contests/$contestId"
        }

        val conditionTf = task.taskFiles.firstOrNull { it.type == trik.testsys.webapp.backoffice.data.entity.impl.TaskFile.TaskFileType.CONDITION } ?: run {
            redirectAttributes.addMessage("У задания нет Условия.")
            return "redirect:/user/student/contests/$contestId/tasks/$taskId"
        }
        val file = fileManager.getTaskFile(conditionTf) ?: run {
            redirectAttributes.addMessage("Файл условия отсутствует на сервере.")
            return "redirect:/user/student/contests/$contestId/tasks/$taskId"
        }

        val bytes = file.readBytes()
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"${task.id}.pdf\"")
            .header("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .header("Content-Length", bytes.size.toString())
            .body(bytes)
    }
}


