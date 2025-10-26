package trik.testsys.webapp.backoffice.controller.impl.user.developer

import jakarta.servlet.http.HttpSession
import org.slf4j.LoggerFactory
import java.time.Instant
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webapp.backoffice.controller.AbstractUserController
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.entity.impl.Task
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.service.ContestService
import trik.testsys.webapp.backoffice.data.service.SolutionService
import trik.testsys.webapp.backoffice.data.service.TaskService
import trik.testsys.webapp.backoffice.data.service.VerdictService
import trik.testsys.webapp.backoffice.data.service.UserGroupService
import trik.testsys.webapp.backoffice.data.service.impl.taskFile.ConditionFileService
import trik.testsys.webapp.backoffice.data.service.impl.taskFile.ExerciseFileService
import trik.testsys.webapp.backoffice.data.service.impl.taskFile.PolygonFileService
import trik.testsys.webapp.backoffice.data.service.impl.taskFile.SolutionFileService
import trik.testsys.webapp.backoffice.service.FileManager
import trik.testsys.webapp.backoffice.service.Grader
import trik.testsys.webapp.backoffice.utils.addMessage

@Controller
@RequestMapping("/user/developer")
class DeveloperTaskController(
    private val contestService: ContestService,
    private val taskService: TaskService,
    private val solutionService: SolutionService,
    private val fileManager: FileManager,
    private val grader: Grader,
    @Value("\${trik.testsys.trik-studio.container.name}")
    private val trikStudioContainerName: String,
    private val verdictService: VerdictService,
    private val userGroupService: UserGroupService,

    private val conditionFileService: ConditionFileService,
    private val exerciseFileService: ExerciseFileService,
    private val polygonFileService: PolygonFileService,
    private val solutionFileService: SolutionFileService,
) : AbstractUserController() {

    @GetMapping("/tasks")
    fun tasksPage(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val createdTasks = taskService.findByDeveloper(developer).sortedBy { it.id }
        val sharedTasks = taskService
            .findForUser(developer)
            .filter { it.developer?.id != developer.id }
            .sortedBy { it.id }

        setupModel(model, session, developer)
        model.addAttribute("tasks", createdTasks)
        model.addAttribute("sharedTasks", sharedTasks)

        return "developer/tasks"
    }

    @GetMapping("/tasks/create")
    fun taskCreateForm(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        setupModel(model, session, developer)

        return "developer/task-create"
    }

    @PostMapping("/tasks/create")
    fun createTask(
        @RequestParam name: String,
        @RequestParam(required = false) info: String?,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            redirectAttributes.addMessage("Название не может быть пустым.")
            return "redirect:/user/developer/tasks"
        }

        val task = Task().also {
            it.name = trimmedName
            it.developer = developer
            it.info = info?.takeIf { s -> s.isNotBlank() }
        }
        taskService.save(task)
        redirectAttributes.addMessage("Задача создана (id=${task.id}).")
        return "redirect:/user/developer/tasks"
    }

    @GetMapping("/tasks/{id}")
    fun viewTask(
        @PathVariable id: Long,
        model: Model,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val task = taskService.findById(id) ?: run {
            redirectAttributes.addMessage("Задача не найдена.")
            return "redirect:/user/developer/tasks"
        }
        if (task.developer?.id != developer.id) {
            redirectAttributes.addMessage("У вас нет доступа к этой Задаче.")
            return "redirect:/user/developer/tasks"
        }

        val attachedConditions = task.data.conditionFileIds.let { conditionFileService.findAllById(it) }
        val attachedExercises = task.data.exerciseFileIds.let { exerciseFileService.findAllById(it) }
        val attachedPolygons = task.data.polygonFileIds.let { polygonFileService.findAllById(it) }
        val attachedSolutions = task.data.solutionFileDataById.keys.let { solutionFileService.findAllById(it) }

        val ownedConditions = conditionFileService.findByDeveloper(developer.id!!).filterNot { task.data.conditionFileIds.contains(it.id) }
        val ownedExercises = exerciseFileService.findByDeveloper(developer.id!!).filterNot { task.data.exerciseFileIds.contains(it.id) }
        val ownedPolygons = polygonFileService.findByDeveloper(developer.id!!).filterNot { task.data.polygonFileIds.contains(it.id) }
        val ownedSolutions = solutionFileService.findByDeveloper(developer.id!!).filterNot { task.data.solutionFileDataById.keys.contains(it.id) }

        val hasPolygon = attachedPolygons.isNotEmpty()
        val hasSolution = attachedSolutions.isNotEmpty()

        val allContests = contestService.findAll()

        val isUsedInAnyContest = allContests.asSequence()
            .any { c -> c.tasks.any { it.id == task.id } }

        val attachedContests = allContests
            .filter { c -> c.tasks.any { it.id == task.id } }
            .sortedBy { it.id }

        val lastTestStatus = when (task.testingStatus) {
            Task.TestingStatus.NOT_TESTED -> "Не запускалось"
            Task.TestingStatus.TESTING -> "В процессе"
            Task.TestingStatus.PASSED -> "Пройдено"
            Task.TestingStatus.FAILED -> "Не пройдено"
        }

        val testSolutions = task.solutions.filter { it.contest == null }
            .sortedByDescending { it.id }

        val resultsAvailability = testSolutions.associate { s ->
            val hasVerdicts = fileManager.getVerdicts(s).isNotEmpty()
            val hasRecordings = fileManager.getRecording(s).isNotEmpty()
            (s.id!!) to (hasVerdicts || hasRecordings)
        }

        setupModel(model, session, developer)

        model.addAttribute("attachedConditions", attachedConditions)
        model.addAttribute("attachedExercises", attachedExercises)
        model.addAttribute("attachedPolygons", attachedPolygons)
        model.addAttribute("attachedSolutions", attachedSolutions)

        model.addAttribute("availableConditions", ownedConditions)
        model.addAttribute("availableExercises", ownedExercises)
        model.addAttribute("availablePolygons", ownedPolygons)
        model.addAttribute("availableSolutions", ownedSolutions)

        model.addAttribute("task", task)
        model.addAttribute("isOwner", true)
        model.addAttribute("isTesting", task.testingStatus == Task.TestingStatus.TESTING)
        model.addAttribute("testReady", hasPolygon && hasSolution)
        model.addAttribute("numPolygons", attachedPolygons.size)
        model.addAttribute("numSolutions", attachedSolutions.size)
        model.addAttribute("testStatus", lastTestStatus)
        model.addAttribute("isUsedInAnyContest", isUsedInAnyContest)
        model.addAttribute("attachedContests", attachedContests)
        model.addAttribute("hasAnySolutions", testSolutions.isNotEmpty())
        model.addAttribute("resultsAvailable", resultsAvailability)
        model.addAttribute(
            "availableUserGroups",
            userGroupService.findByMember(developer)
                .filterNot { g -> task.userGroups.any { it.id == g.id } }
                .sortedBy { it.id }
        )

        return "developer/task"
    }

    @GetMapping("/tasks/{id}/tests")
    fun taskTests(
        @PathVariable id: Long,
        model: Model,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val task = taskService.findById(id) ?: run {
            redirectAttributes.addMessage("Задача не найдена.")
            return "redirect:/user/developer/tasks"
        }
        if (task.developer?.id != developer.id) {
            redirectAttributes.addMessage("У вас нет доступа к этой Задаче.")
            return "redirect:/user/developer/tasks"
        }

        val testSolutions = task.solutions.filter { it.contest == null }
            .sortedByDescending { it.id }

        val verdicts = verdictService.findAllBySolutions(testSolutions)
        val verdictsBySolutionIds = verdicts.associateBy { it.solutionId }

        val resultsAvailability = testSolutions.associate { s ->
            val hasVerdicts = fileManager.getVerdicts(s).isNotEmpty()
            val hasRecordings = fileManager.getRecording(s).isNotEmpty()
            (s.id!!) to (hasVerdicts || hasRecordings)
        }

        setupModel(model, session, developer)
        model.addAttribute("task", task)
        model.addAttribute("solutions", testSolutions)
        model.addAttribute("verdicts", verdictsBySolutionIds)
        model.addAttribute("resultsAvailable", resultsAvailability)

        return "developer/task-tests"
    }

    @GetMapping("/tasks/{taskId}/tests/{solutionId}/download")
    fun downloadTestResults(
        @PathVariable("taskId") taskId: Long,
        @PathVariable("solutionId") solutionId: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes,
    ): Any {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val task = taskService.findById(taskId) ?: run {
            redirectAttributes.addMessage("Задача не найдена.")
            return "redirect:/user/developer/tasks"
        }
        if (task.developer?.id != developer.id) {
            redirectAttributes.addMessage("У вас нет доступа к этой Задаче.")
            return "redirect:/user/developer/tasks"
        }

        val solution = solutionService.findById(solutionId) ?: run {
            redirectAttributes.addMessage("Решение не найдено.")
            return "redirect:/user/developer/tasks/$taskId/tests"
        }
        if (solution.task.id != task.id || solution.contest != null) {
            redirectAttributes.addMessage("Решение не относится к данному тестированию Задачи.")
            return "redirect:/user/developer/tasks/$taskId/tests"
        }
        if (solution.createdBy.id != developer.id) {
            redirectAttributes.addMessage("У вас нет доступа к данному Решению.")
            return "redirect:/user/developer/tasks/$taskId/tests"
        }

        val hasAnyResults = fileManager.getVerdicts(solution).isNotEmpty() || fileManager.getRecording(solution).isNotEmpty()
        if (!hasAnyResults) {
            redirectAttributes.addMessage("Результаты для данного Решения отсутствуют.")
            return "redirect:/user/developer/tasks/$taskId/tests"
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

    @PostMapping("/tasks/{id}/groups/add")
    fun addUserGroup(
        @PathVariable id: Long,
        @RequestParam("groupId") groupId: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val task = taskService.findById(id) ?: run {
            redirectAttributes.addMessage("Задача не найдена.")
            return "redirect:/user/developer/tasks"
        }
        if (task.developer?.id != developer.id) {
            redirectAttributes.addMessage("Доступно только владельцу.")
            return "redirect:/user/developer/tasks/$id"
        }

        val group = userGroupService.findById(groupId)
        if (group == null) {
            redirectAttributes.addMessage("Группа не найдена.")
            return "redirect:/user/developer/tasks/$id"
        }

        val added = task.userGroups.add(group)
        taskService.save(task)
        if (added) redirectAttributes.addMessage("Группа добавлена к доступу.") else redirectAttributes.addMessage("Группа уже имеет доступ.")
        return "redirect:/user/developer/tasks/$id"
    }

    @PostMapping("/tasks/{id}/update")
    fun updateTask(
        @PathVariable id: Long,
        @RequestParam name: String,
        @RequestParam(required = false) info: String?,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val task = taskService.findById(id) ?: run {
            redirectAttributes.addMessage("Задача не найдена.")
            return "redirect:/user/developer/tasks"
        }
        if (task.developer?.id != developer.id) {
            redirectAttributes.addMessage("Редактирование доступно только владельцу.")
            return "redirect:/user/developer/tasks/$id"
        }

        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            redirectAttributes.addMessage("Название не может быть пустым.")
            return "redirect:/user/developer/tasks/$id"
        }

        task.name = trimmedName
        task.info = info?.takeIf { it.isNotBlank() }
        taskService.save(task)
        redirectAttributes.addMessage("Данные Задачи обновлены.")
        return "redirect:/user/developer/tasks/$id"
    }

    @PostMapping("/tasks/{id}/files/condition/attach")
    fun attachConditionFile(
        @PathVariable id: Long,
        @RequestParam("conditionId") conditionFileId: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val task = taskService.findById(id) ?: run {
            redirectAttributes.addMessage("Задача не найдена.")
            return "redirect:/user/developer/tasks"
        }
        if (task.developer?.id != developer.id) {
            redirectAttributes.addMessage("Изменение доступов доступно только владельцу.")
            return "redirect:/user/developer/tasks/$id"
        }

        val conditionFile = conditionFileService.findById(conditionFileId) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/tasks/$id"
        }
        if (conditionFile.developerId != developer.id) {
            redirectAttributes.addMessage("Можно прикреплять только свои файлы.")
            return "redirect:/user/developer/tasks/$id"
        }

        task.data.conditionFileIds.clear()
        task.data.conditionFileIds.add(conditionFileId)
        taskService.save(task)

        conditionFile.data.attachedTaskIds.add(task.id!!)
        conditionFileService.save(conditionFile)

        redirectAttributes.addMessage("Файл прикреплён к Задаче.")
        return "redirect:/user/developer/tasks/$id"
    }

    @PostMapping("/tasks/{id}/files/exercise/attach")
    fun attachExerciseFile(
        @PathVariable id: Long,
        @RequestParam("exerciseId") exerciseFileId: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val task = taskService.findById(id) ?: run {
            redirectAttributes.addMessage("Задача не найдена.")
            return "redirect:/user/developer/tasks"
        }
        if (task.developer?.id != developer.id) {
            redirectAttributes.addMessage("Изменение доступов доступно только владельцу.")
            return "redirect:/user/developer/tasks/$id"
        }

        val exerciseFile = exerciseFileService.findById(exerciseFileId) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/tasks/$id"
        }
        if (exerciseFile.developerId != developer.id) {
            redirectAttributes.addMessage("Можно прикреплять только свои файлы.")
            return "redirect:/user/developer/tasks/$id"
        }

        task.data.exerciseFileIds.clear()
        task.data.exerciseFileIds.add(exerciseFileId)
        taskService.save(task)

        exerciseFile.data.attachedTaskIds.add(task.id!!)
        exerciseFileService.save(exerciseFile)

        redirectAttributes.addMessage("Файл прикреплён к Задаче.")
        return "redirect:/user/developer/tasks/$id"
    }

    @PostMapping("/tasks/{id}/files/polygon/attach")
    fun attachPolygonFile(
        @PathVariable id: Long,
        @RequestParam("polygonId") polygonFileId: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val task = taskService.findById(id) ?: run {
            redirectAttributes.addMessage("Задача не найдена.")
            return "redirect:/user/developer/tasks"
        }
        if (task.developer?.id != developer.id) {
            redirectAttributes.addMessage("Изменение доступов доступно только владельцу.")
            return "redirect:/user/developer/tasks/$id"
        }

        val polygonFile = polygonFileService.findById(polygonFileId) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/tasks/$id"
        }
        if (polygonFile.developerId != developer.id) {
            redirectAttributes.addMessage("Можно прикреплять только свои файлы.")
            return "redirect:/user/developer/tasks/$id"
        }

        if (task.testingStatus == Task.TestingStatus.TESTING) {
            redirectAttributes.addMessage("Нельзя изменять Полигоны во время тестирования.")
            return "redirect:/user/developer/tasks/$id"
        }

        val usedInContest = contestService.findAll().asSequence().any { c -> c.tasks.any { it.id == task.id } }
        if (usedInContest) {
            redirectAttributes.addMessage("Нельзя изменять Полигоны Задачи, прикреплённой к Туру.")
            return "redirect:/user/developer/tasks/$id"
        }

        task.testingStatus = Task.TestingStatus.NOT_TESTED
        task.data.polygonFileIds.add(polygonFileId)
        taskService.save(task)

        polygonFile.data.attachedTaskIds.add(task.id!!)
        polygonFileService.save(polygonFile)

        redirectAttributes.addMessage("Файл прикреплён к Задаче.")
        return "redirect:/user/developer/tasks/$id"
    }

    @PostMapping("/tasks/{id}/files/solution/attach")
    fun attachSolutionFile(
        @PathVariable id: Long,
        @RequestParam("solutionId") solutionFileId: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val task = taskService.findById(id) ?: run {
            redirectAttributes.addMessage("Задача не найдена.")
            return "redirect:/user/developer/tasks"
        }
        if (task.developer?.id != developer.id) {
            redirectAttributes.addMessage("Изменение доступов доступно только владельцу.")
            return "redirect:/user/developer/tasks/$id"
        }

        val solutionFile = solutionFileService.findById(solutionFileId) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/tasks/$id"
        }
        if (solutionFile.developerId != developer.id) {
            redirectAttributes.addMessage("Можно прикреплять только свои файлы.")
            return "redirect:/user/developer/tasks/$id"
        }

        if (task.testingStatus == Task.TestingStatus.TESTING) {
            redirectAttributes.addMessage("Нельзя изменять файлы во время тестирования.")
            return "redirect:/user/developer/tasks/$id"
        }

        val usedInContest = contestService.findAll().asSequence().any { c -> c.tasks.any { it.id == task.id } }
        if (usedInContest) {
            redirectAttributes.addMessage("Нельзя изменять Полигоны Задачи, прикреплённой к Туру.")
            return "redirect:/user/developer/tasks/$id"
        }

        task.testingStatus = Task.TestingStatus.NOT_TESTED

        val solutionFileData = Task.SolutionFileData(
            solutionFile.solutionType,
            null,
            0L
        )
        task.data.solutionFileDataById[solutionFileId] = solutionFileData
        taskService.save(task)

        solutionFile.data.attachedTaskIds.add(task.id!!)
        solutionFileService.save(solutionFile)

        redirectAttributes.addMessage("Файл прикреплён к Задаче.")
        return "redirect:/user/developer/tasks/$id"
    }

    @PostMapping("/tasks/{taskId}/files/condition/{fileId}/detach")
    fun detachConditionFile(
        @PathVariable taskId: Long,
        @PathVariable fileId: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val task = taskService.findById(taskId) ?: run {
            redirectAttributes.addMessage("Задача не найдена.")
            return "redirect:/user/developer/tasks"
        }
        if (task.developer?.id != developer.id) {
            redirectAttributes.addMessage("Изменение доступов доступно только владельцу.")
            return "redirect:/user/developer/tasks/$taskId"
        }

        val conditionFile = conditionFileService.findById(fileId) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/tasks/$taskId"
        }

        task.data.conditionFileIds.remove(fileId)
        taskService.save(task)

        conditionFile.data.attachedTaskIds.remove(taskId)
        conditionFileService.save(conditionFile)

        redirectAttributes.addMessage("Файл откреплён от Задачи.")
        return "redirect:/user/developer/tasks/$taskId"
    }

    @PostMapping("/tasks/{taskId}/files/exercise/{fileId}/detach")
    fun detachExerciseFile(
        @PathVariable taskId: Long,
        @PathVariable fileId: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val task = taskService.findById(taskId) ?: run {
            redirectAttributes.addMessage("Задача не найдена.")
            return "redirect:/user/developer/tasks"
        }
        if (task.developer?.id != developer.id) {
            redirectAttributes.addMessage("Изменение доступов доступно только владельцу.")
            return "redirect:/user/developer/tasks/$taskId"
        }

        val exerciseFile = exerciseFileService.findById(fileId) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/tasks/$taskId"
        }

        task.data.exerciseFileIds.remove(fileId)
        taskService.save(task)

        exerciseFile.data.attachedTaskIds.remove(taskId)
        exerciseFileService.save(exerciseFile)

        redirectAttributes.addMessage("Файл откреплён от Задачи.")
        return "redirect:/user/developer/tasks/$taskId"
    }

    @PostMapping("/tasks/{taskId}/files/polygon/{fileId}/detach")
    fun detachPolygonFile(
        @PathVariable taskId: Long,
        @PathVariable fileId: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val task = taskService.findById(taskId) ?: run {
            redirectAttributes.addMessage("Задача не найдена.")
            return "redirect:/user/developer/tasks"
        }
        if (task.developer?.id != developer.id) {
            redirectAttributes.addMessage("Изменение доступов доступно только владельцу.")
            return "redirect:/user/developer/tasks/$taskId"
        }

        if (task.testingStatus == Task.TestingStatus.TESTING) {
            redirectAttributes.addMessage("Нельзя откреплять Полигоны во время тестирования.")
            return "redirect:/user/developer/tasks/$taskId"
        }

        val usedInContest = contestService.findAll().asSequence().any { c -> c.tasks.any { it.id == task.id } }
        if (usedInContest) {
            redirectAttributes.addMessage("Нельзя откреплять Полигоны от Задачи, прикреплённой к Туру.")
            return "redirect:/user/developer/tasks/$taskId"
        }

        val polygonFile = polygonFileService.findById(fileId) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/tasks/$taskId"
        }

        task.testingStatus = Task.TestingStatus.NOT_TESTED
        task.data.polygonFileIds.remove(fileId)
        taskService.save(task)

        polygonFile.data.attachedTaskIds.remove(taskId)
        polygonFileService.save(polygonFile)

        redirectAttributes.addMessage("Файл откреплён от Задачи.")
        return "redirect:/user/developer/tasks/$taskId"
    }

    @PostMapping("/tasks/{taskId}/files/solution/{fileId}/detach")
    fun detachSolutionFile(
        @PathVariable taskId: Long,
        @PathVariable fileId: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val task = taskService.findById(taskId) ?: run {
            redirectAttributes.addMessage("Задача не найдена.")
            return "redirect:/user/developer/tasks"
        }
        if (task.developer?.id != developer.id) {
            redirectAttributes.addMessage("Изменение доступов доступно только владельцу.")
            return "redirect:/user/developer/tasks/$taskId"
        }

        if (task.testingStatus == Task.TestingStatus.TESTING) {
            redirectAttributes.addMessage("Нельзя откреплять Эталонные решения во время тестирования.")
            return "redirect:/user/developer/tasks/$taskId"
        }

        val usedInContest = contestService.findAll().asSequence().any { c -> c.tasks.any { it.id == task.id } }
        if (usedInContest) {
            redirectAttributes.addMessage("Нельзя откреплять Эталонные решения от Задачи, прикреплённой к Туру.")
            return "redirect:/user/developer/tasks/$taskId"
        }

        val solutionFile = solutionFileService.findById(fileId) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/tasks/$taskId"
        }

        task.testingStatus = Task.TestingStatus.NOT_TESTED
        task.data.solutionFileDataById.remove(fileId)
        taskService.save(task)

        solutionFile.data.attachedTaskIds.remove(taskId)
        solutionFileService.save(solutionFile)

        redirectAttributes.addMessage("Файл откреплён от Задачи.")
        return "redirect:/user/developer/tasks/$taskId"
    }

    @PostMapping("/tasks/{id}/test")
    fun testTask(
        @PathVariable id: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val task = taskService.findById(id) ?: run {
            redirectAttributes.addMessage("Задача не найдена.")
            return "redirect:/user/developer/tasks"
        }
        if (task.developer?.id != developer.id) {
            redirectAttributes.addMessage("Доступно только владельцу.")
            return "redirect:/user/developer/tasks/$id"
        }

        val usedInContest = contestService.findAll().any { c -> c.tasks.any { it.id == task.id } }
        if (usedInContest) {
            redirectAttributes.addMessage("Нельзя запускать тестирование для Задачи, прикреплённой к Туру.")
            return "redirect:/user/developer/tasks/$id"
        }

        if (task.testingStatus == Task.TestingStatus.TESTING) {
            redirectAttributes.addMessage("Задача уже тестируется.")
            return "redirect:/user/developer/tasks/$id"
        }

        val hasPolygons = task.data.polygonFileIds.isNotEmpty()
        val hasSolutions = task.data.solutionFileDataById.isNotEmpty()
        if (!hasPolygons || !hasSolutions) {
            redirectAttributes.addMessage("Для тестирования требуется минимум один Файл типа Полигон и один Файл типа Эталонное Решение.")
            return "redirect:/user/developer/tasks/$id"
        }

        val solutionFiles = solutionFileService.findAllById(task.data.solutionFileDataById.keys)
        val polygonFiles = polygonFileService.findAllById(task.data.polygonFileIds)

        if (solutionFiles.isEmpty() || polygonFiles.isEmpty()) {
            redirectAttributes.addMessage("Для тестирования требуется минимум один Полигон и один Эталонное Решение.")
            return "redirect:/user/developer/tasks/$id"
        }

        // Collect all solution file updates first
        val solutionFileUpdates = mutableMapOf<Long, Pair<Long, Long>>()
        val solutionsToGrade = mutableListOf<Solution>()

        // For each solution file, create a synthetic Solution and prepare for grading
        solutionFiles.forEach { solutionTf ->
            val solution = Solution().also {
                it.createdBy = developer
                it.contest = null
                it.task = task
                it.type = solutionTf.solutionType
                it.info = "Тестирование Эталонного решения (id=${solutionTf.id}, type=${it.type}) для Задачи (id=${task.id})."
            }

            logger.debug("Calling solutionService.save(id=${solution.id}) in DeveloperTaskController.testTask")
            val saved = solutionService.save(solution)

            val solutionSource = fileManager.getSolutionFile(solutionTf)
                ?: run {
                    solutionService.delete(saved)
                    redirectAttributes.addMessage("Файл эталонного решения недоступен на сервере.")
                    return "redirect:/user/developer/tasks/$id"
                }

            val ok = fileManager.saveSolution(saved, solutionSource)
            if (!ok) {
                solutionService.delete(saved)
                redirectAttributes.addMessage("Не удалось подготовить файл решения для тестирования.")
                return "redirect:/user/developer/tasks/$id"
            }

            solutionFileUpdates[solutionTf.id!!] = Pair(saved.id!!, 0L)
            solutionsToGrade.add(saved)
        }

        // Mark testing in progress and update task data in a single save
        task.testingStatus = Task.TestingStatus.TESTING
        solutionFileUpdates.forEach { entry ->
            val solutionFileId = entry.key
            val (solutionId, score) = entry.value
            task.data.solutionFileDataById[solutionFileId]?.lastSolutionId = solutionId
            task.data.solutionFileDataById[solutionFileId]?.lastTestScore = score
        }
        taskService.save(task)

        // Send all solutions for grading after task is saved
        solutionsToGrade.forEach { solution ->
            grader.sendToGrade(solution, Grader.GradingOptions(shouldRecordRun = true, trikStudioVersion = trikStudioContainerName))
        }

        redirectAttributes.addMessage("Задача отправлена на тестирование.")
        return "redirect:/user/developer/tasks/$id"
    }

    @PostMapping("/tasks/{id}/delete")
    fun deleteTask(
        @PathVariable id: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val task = taskService.findById(id) ?: run {
            redirectAttributes.addMessage("Задача не найдена.")
            return "redirect:/user/developer/tasks"
        }
        if (task.developer?.id != developer.id) {
            redirectAttributes.addMessage("Удаление доступно только владельцу.")
            return "redirect:/user/developer/tasks/$id"
        }

        val usedInContest = contestService.findAll().any { c -> c.tasks.any { it.id == task.id } }
        if (usedInContest) {
            redirectAttributes.addMessage("Нельзя удалить Задачу, прикреплённую к Туру.")
            return "redirect:/user/developer/tasks/$id"
        }

        if (task.solutions.any { it.contest != null }) {
            redirectAttributes.addMessage("Нельзя удалить Задачу, по которой есть Решения.")
            return "redirect:/user/developer/tasks/$id"
        }

        val attachedConditions = task.data.conditionFileIds.let { conditionFileService.findAllById(it) }
        val attachedExercises = task.data.exerciseFileIds.let { exerciseFileService.findAllById(it) }
        val attachedPolygons = task.data.polygonFileIds.let { polygonFileService.findAllById(it) }
        val attachedSolutions = task.data.solutionFileDataById.keys.let { solutionFileService.findAllById(it) }

        attachedConditions.forEach { it.data.attachedTaskIds.remove(task.id) }
        attachedExercises.forEach { it.data.attachedTaskIds.remove(task.id) }
        attachedPolygons.forEach { it.data.attachedTaskIds.remove(task.id) }
        attachedSolutions.forEach { it.data.attachedTaskIds.remove(task.id) }

        conditionFileService.saveAll(attachedConditions)
        exerciseFileService.saveAll(attachedExercises)
        polygonFileService.saveAll(attachedPolygons)
        solutionFileService.saveAll(attachedSolutions)

        taskService.delete(task)
        redirectAttributes.addMessage("Задача удалена.")
        return "redirect:/user/developer/tasks"
    }

    @PostMapping("/tasks/{id}/solution-score")
    fun updateSolutionScore(
        @PathVariable id: Long,
        @RequestParam solutionFileId: Long,
        @RequestParam score: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val task = taskService.findById(id) ?: run {
            redirectAttributes.addMessage("Задача не найдена.")
            return "redirect:/user/developer/tasks"
        }
        if (task.developer?.id != developer.id) {
            redirectAttributes.addMessage("У вас нет доступа к этой Задаче.")
            return "redirect:/user/developer/tasks"
        }

        if (task.testingStatus == Task.TestingStatus.TESTING) {
            redirectAttributes.addMessage("Нельзя изменять баллы во время тестирования.")
            return "redirect:/user/developer/tasks/$id"
        }

        val allContests = contestService.findAll()
        val isUsedInAnyContest = allContests.asSequence()
            .any { c -> c.tasks.any { it.id == task.id } }

        if (isUsedInAnyContest) {
            redirectAttributes.addMessage("Нельзя изменять баллы для задачи, используемой в соревновании.")
            return "redirect:/user/developer/tasks/$id"
        }

        val solutionFileData = task.data.solutionFileDataById[solutionFileId]
        if (solutionFileData == null) {
            redirectAttributes.addMessage("Решение не найдено в задаче.")
            return "redirect:/user/developer/tasks/$id"
        }
        
        solutionFileData.score = score
        taskService.save(task)

        redirectAttributes.addMessage("Балл для решения обновлён.")
        return "redirect:/user/developer/tasks/$id"
    }

    private data class TaskFileListItem(
        val id: Long,
        val name: String?,
        val info: String?,
        val createdAt: Instant?,
        val localizedType: String,
    )

    companion object {

        private val logger = LoggerFactory.getLogger(DeveloperTaskController::class.java)
    }
}
