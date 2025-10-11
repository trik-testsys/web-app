package trik.testsys.webapp.backoffice.controller.impl.user.judge

import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webapp.backoffice.controller.AbstractUserController
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.service.SolutionService
import trik.testsys.webapp.backoffice.data.service.VerdictService
import trik.testsys.webapp.backoffice.service.FileManager
import trik.testsys.webapp.backoffice.service.Grader
import trik.testsys.webapp.backoffice.utils.addMessage

@Controller
@RequestMapping("/user/judge")
class JudgeController(
    private val solutionService: SolutionService,
    private val verdictService: VerdictService,
    private val fileManager: FileManager,
    private val grader: Grader,
    @Value("\${trik.testsys.trik-studio.container.name}") private val trikStudioContainerName: String,
) : AbstractUserController() {

    @GetMapping("/solutions")
    fun solutionsPage(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val judge = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!judge.privileges.contains(User.Privilege.JUDGE)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val allSolutions = solutionService.findAll().sortedByDescending { it.id }

        val studentSolutions = allSolutions.filter { it.createdBy.privileges.contains(User.Privilege.STUDENT) }
        val judgeSolutions = allSolutions.filter { it.createdBy.id == judge.id }

        val verdicts = verdictService.findAllBySolutions(studentSolutions + judgeSolutions)
        val verdictsBySolutionId = verdicts.associateBy { it.solutionId }

        // Compute viewer/admin/group for student solutions only
        val viewerBySolutionId = mutableMapOf<Long, Long?>()
        val adminBySolutionId = mutableMapOf<Long, Long?>()
        val groupBySolutionId = mutableMapOf<Long, Long?>()
        studentSolutions.forEach { s ->
            val contest = s.contest
            val student = s.createdBy
            val group = if (contest != null) {
                student.memberedStudentGroups.firstOrNull { sg -> sg.contests.any { it.id == contest.id } }
            } else null
            val admin = group?.owner
            val viewer = admin?.viewer
            val sid = s.id ?: return@forEach
            groupBySolutionId[sid] = group?.id
            adminBySolutionId[sid] = admin?.id
            viewerBySolutionId[sid] = viewer?.id
        }

        val resultsAvailability = (studentSolutions + judgeSolutions).associate { s ->
            val hasVerdicts = fileManager.hasAnyVerdict(s)
            val hasRecordings = fileManager.hasAnyRecording(s)
            (s.id!!) to (hasVerdicts || hasRecordings)
        }

        val solutionFileAvailability = (studentSolutions + judgeSolutions).associate { s ->
            (s.id!!) to fileManager.hasSolution(s)
        }

        setupModel(model, session, judge)
        model.addAttribute("studentSolutions", studentSolutions)
        model.addAttribute("judgeSolutions", judgeSolutions)
        model.addAttribute("verdicts", verdictsBySolutionId)
        model.addAttribute("viewerBySolutionId", viewerBySolutionId)
        model.addAttribute("adminBySolutionId", adminBySolutionId)
        model.addAttribute("groupBySolutionId", groupBySolutionId)
        model.addAttribute("resultsAvailable", resultsAvailability)
        model.addAttribute("solutionFileAvailable", solutionFileAvailability)

        return "judge/solutions"
    }

    @GetMapping("/solutions/by-name")
    fun redirectByName(
        @RequestParam("name") name: String,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val judge = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"
        if (!judge.privileges.contains(User.Privilege.JUDGE)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val id = name.toLongOrNull()
        if (id == null) {
            redirectAttributes.addMessage("Некорректный идентификатор решения.")
            return "redirect:/user/judge/solutions"
        }
        return "redirect:/user/judge/solutions/$id"
    }

    @GetMapping("/solutions/{solutionId}")
    fun solutionDetails(
        @PathVariable("solutionId") solutionId: Long,
        model: Model,
        session: HttpSession,
        redirectAttributes: RedirectAttributes,
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val judge = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!judge.privileges.contains(User.Privilege.JUDGE)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val solution = solutionService.findById(solutionId) ?: run {
            redirectAttributes.addMessage("Решение не найдено.")
            return "redirect:/user/judge/solutions"
        }

        val verdicts = verdictService.findAllForSolution(solutionId).sortedByDescending { it.id }

        setupModel(model, session, judge)
        model.addAttribute("solution", solution)
        model.addAttribute("verdicts", verdicts)
        model.addAttribute("resultsAvailable", (fileManager.getVerdicts(solution).isNotEmpty() || fileManager.getRecording(solution).isNotEmpty()))
        model.addAttribute("solutionFileAvailable", (fileManager.getSolution(solution) != null))
        return "judge/solution"
    }

    @PostMapping("/solutions/{solutionId}/relevant")
    fun createRelevantVerdict(
        @PathVariable("solutionId") solutionId: Long,
        @RequestParam("score") score: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes,
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val judge = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!judge.privileges.contains(User.Privilege.JUDGE)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val solution = solutionService.findById(solutionId) ?: run {
            redirectAttributes.addMessage("Решение не найдено.")
            return "redirect:/user/judge/solutions"
        }

        verdictService.createNewForSolution(solution, score)
        solutionService.save(solution)
        redirectAttributes.addMessage("Релевантный вердикт обновлён.")
        return "redirect:/user/judge/solutions/$solutionId"
    }

    @GetMapping("/solutions/{solutionId}/download")
    fun downloadResults(
        @PathVariable("solutionId") solutionId: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes,
    ): Any {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val judge = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!judge.privileges.contains(User.Privilege.JUDGE)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val solution = solutionService.findById(solutionId) ?: run {
            redirectAttributes.addMessage("Решение не найдено.")
            return "redirect:/user/judge/solutions"
        }

        val hasAnyResults = fileManager.getVerdicts(solution).isNotEmpty() || fileManager.getRecording(solution).isNotEmpty()
        if (!hasAnyResults) {
            redirectAttributes.addMessage("Результаты для данного Решения отсутствуют.")
            return "redirect:/user/judge/solutions"
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

    @GetMapping("/solutions/{solutionId}/file")
    fun downloadSolutionFile(
        @PathVariable("solutionId") solutionId: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes,
    ): Any {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val judge = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!judge.privileges.contains(User.Privilege.JUDGE)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val solution = solutionService.findById(solutionId) ?: run {
            redirectAttributes.addMessage("Решение не найдено.")
            return "redirect:/user/judge/solutions"
        }

        val file = fileManager.getSolution(solution) ?: run {
            redirectAttributes.addMessage("Файл посылки отсутствует на сервере.")
            return "redirect:/user/judge/solutions"
        }

        val bytes = file.readBytes()
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"${file.name}\"")
            .header("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .header("Content-Length", bytes.size.toString())
            .body(bytes)
    }

    @PostMapping("/solutions/{solutionId}/resubmit")
    fun resubmitSolution(
        @PathVariable("solutionId") solutionId: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes,
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val judge = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!judge.privileges.contains(User.Privilege.JUDGE)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val original = solutionService.findById(solutionId) ?: run {
            redirectAttributes.addMessage("Решение не найдено.")
            return "redirect:/user/judge/solutions"
        }

        // Create a cloned solution owned by judge
        val cloned = Solution().also {
            it.createdBy = judge
            it.contest = original.contest
            it.task = original.task
            it.type = original.type
        }
        val saved = solutionService.save(cloned)

        // Copy original solution file to the cloned one
        val sourceFile = fileManager.getSolution(original)
        if (sourceFile == null) {
            redirectAttributes.addMessage("Файл исходного решения отсутствует на сервере.")
            return "redirect:/user/judge/solutions"
        }
        val copied = fileManager.saveSolution(saved, sourceFile)
        if (!copied) {
            redirectAttributes.addMessage("Не удалось подготовить файл решения для перезапуска.")
            return "redirect:/user/judge/solutions"
        }

        return try {
            grader.sendToGrade(saved, Grader.GradingOptions(shouldRecordRun = true, trikStudioVersion = trikStudioContainerName))
            redirectAttributes.addMessage("Создана новая посылка и отправлена на проверку.")
            "redirect:/user/judge/solutions"
        } catch (e: Exception) {
            redirectAttributes.addMessage("Не удалось отправить на проверку: ${e.message}")
            "redirect:/user/judge/solutions"
        }
    }
}


