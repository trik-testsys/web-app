package trik.testsys.webapp.backoffice.controller.impl.user.judge

import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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
import java.time.LocalDate
import java.time.ZoneOffset
import trik.testsys.webapp.backoffice.controller.AbstractUserController
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.service.SolutionService
import trik.testsys.webapp.backoffice.data.service.VerdictService
import trik.testsys.webapp.backoffice.service.FileManager
import trik.testsys.webapp.backoffice.service.Grader
import trik.testsys.webapp.backoffice.data.repository.support.SolutionSpecifications
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
    fun solutionsPage(
        model: Model,
        session: HttpSession,
        redirectAttributes: RedirectAttributes,
        @RequestParam("studentId", required = false) studentId: Long?,
        @RequestParam("groupId", required = false) groupId: Long?,
        @RequestParam("adminId", required = false) adminId: Long?,
        @RequestParam("viewerId", required = false) viewerId: Long?,
        @RequestParam("fromDate", required = false) fromDate: String?,
        @RequestParam("toDate", required = false) toDate: String?,
        @RequestParam("page", defaultValue = "0") page: Int,
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val judge = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!judge.privileges.contains(User.Privilege.JUDGE)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        // Judge's own resubmits — always shown (small set, no pagination needed)
        val judgeSolutions = solutionService.findAll(SolutionSpecifications.createdBy(judge.id!!))
            .sortedByDescending { it.id }
        val judgeVerdicts = verdictService.findAllBySolutions(judgeSolutions)
        val judgeVerdictsBySolutionId = judgeVerdicts.associateBy { it.solutionId }
        val judgeResultsAvailable = judgeSolutions.associate { s ->
            (s.id!!) to (fileManager.hasAnyVerdict(s) || fileManager.hasAnyRecording(s))
        }
        val judgeSolutionFileAvailable = judgeSolutions.associate { s ->
            (s.id!!) to fileManager.hasSolution(s)
        }

        setupModel(model, session, judge)
        model.addAttribute("judgeSolutions", judgeSolutions)
        model.addAttribute("judgeVerdicts", judgeVerdictsBySolutionId)
        model.addAttribute("judgeResultsAvailable", judgeResultsAvailable)
        model.addAttribute("judgeSolutionFileAvailable", judgeSolutionFileAvailable)

        // Filter params — passed back to template to repopulate the form
        model.addAttribute("filterStudentId", studentId)
        model.addAttribute("filterGroupId", groupId)
        model.addAttribute("filterAdminId", adminId)
        model.addAttribute("filterViewerId", viewerId)
        model.addAttribute("filterFromDate", fromDate)
        model.addAttribute("filterToDate", toDate)

        val hasFilters = listOf(studentId, groupId, adminId, viewerId, fromDate, toDate).any { it != null }
        if (!hasFilters) {
            model.addAttribute("hasSearched", false)
            model.addAttribute("studentSolutionsPage", null)
            model.addAttribute("studentSolutions", emptyList<Solution>())
            model.addAttribute("verdicts", emptyMap<Long, Any>())
            model.addAttribute("viewerBySolutionId", emptyMap<Long, Long?>())
            model.addAttribute("adminBySolutionId", emptyMap<Long, Long?>())
            model.addAttribute("groupBySolutionId", emptyMap<Long, Long?>())
            model.addAttribute("resultsAvailable", emptyMap<Long, Boolean>())
            model.addAttribute("solutionFileAvailable", emptyMap<Long, Boolean>())
            model.addAttribute("paginationPages", emptyList<Int>())
            return "judge/solutions"
        }

        val fromInstant = fromDate?.takeIf { it.isNotBlank() }
            ?.let { LocalDate.parse(it).atStartOfDay(ZoneOffset.UTC).toInstant() }
        val toInstant = toDate?.takeIf { it.isNotBlank() }
            ?.let { LocalDate.parse(it).atTime(23, 59, 59).atOffset(ZoneOffset.UTC).toInstant() }

        val pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending())
        val studentSolutionsPage: Page<Solution> = solutionService.findStudentSolutionsPage(
            studentId, groupId, adminId, viewerId, fromInstant, toInstant, pageable
        )
        val studentSolutions = studentSolutionsPage.content

        val verdicts = verdictService.findAllBySolutions(studentSolutions)
        val verdictsBySolutionId = verdicts.associateBy { it.solutionId }

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

        val resultsAvailability = studentSolutions.associate { s ->
            (s.id!!) to (fileManager.hasAnyVerdict(s) || fileManager.hasAnyRecording(s))
        }
        val solutionFileAvailability = studentSolutions.associate { s ->
            (s.id!!) to fileManager.hasSolution(s)
        }

        model.addAttribute("hasSearched", true)
        model.addAttribute("studentSolutionsPage", studentSolutionsPage)
        model.addAttribute("paginationPages", buildPaginationPages(studentSolutionsPage.number, studentSolutionsPage.totalPages))
        model.addAttribute("studentSolutions", studentSolutions)
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

    private fun buildPaginationPages(current: Int, total: Int): List<Int> {
        if (total <= 1) return emptyList()
        val shown = sortedSetOf<Int>()
        shown.add(0)
        if (1 < total) shown.add(1)
        if (total - 2 >= 0) shown.add(total - 2)
        shown.add(total - 1)
        if (current - 1 >= 0) shown.add(current - 1)
        shown.add(current)
        if (current + 1 < total) shown.add(current + 1)
        val result = mutableListOf<Int>()
        val sorted = shown.toList()
        for (i in sorted.indices) {
            if (i > 0 && sorted[i] - sorted[i - 1] > 1) result.add(-1)
            result.add(sorted[i])
        }
        return result
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}


