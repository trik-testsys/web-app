package trik.testsys.webapp.backoffice.controller.impl.user.developer

import jakarta.servlet.http.HttpSession
import java.time.Instant
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webapp.backoffice.controller.AbstractUserController
import trik.testsys.webapp.backoffice.data.entity.impl.Task
import trik.testsys.webapp.backoffice.data.entity.impl.TaskFile
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.service.ContestService
import trik.testsys.webapp.backoffice.data.service.SolutionService
import trik.testsys.webapp.backoffice.data.service.TaskFileService
import trik.testsys.webapp.backoffice.data.service.TaskService
import trik.testsys.webapp.backoffice.utils.addMessage

@Controller
@RequestMapping("/user/developer")
class DeveloperTaskController(
    private val contestService: ContestService,
    private val taskService: TaskService,
    private val taskFileService: TaskFileService,
    private val solutionService: SolutionService,
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
        redirectAttributes.addMessage("Задача создана (id=${'$'}{task.id}).")
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

        val attachedTaskFiles = task.taskFiles.sortedBy { it.id }
        val ownedTaskFiles = taskFileService.findByDeveloper(developer).sortedBy { it.id }
        val availableTaskFiles = ownedTaskFiles.filterNot { otf -> attachedTaskFiles.any { it.id == otf.id } }

        fun toTaskFileListItem(tf: TaskFile) = TaskFileListItem(
            id = tf.id!!,
            name = tf.name,
            info = tf.info,
            createdAt = tf.createdAt,
            localizedType = when (tf.type) {
                TaskFile.TaskFileType.POLYGON -> "Полигон"
                TaskFile.TaskFileType.EXERCISE -> "Упражнение"
                TaskFile.TaskFileType.SOLUTION -> "Эталонное Решение"
                TaskFile.TaskFileType.CONDITION -> "Условие"
                else -> tf.type?.name ?: "—"
            }
        )

        val attachedItems = attachedTaskFiles.map(::toTaskFileListItem)
        val availableItems = availableTaskFiles.map(::toTaskFileListItem)

        val hasPolygon = task.taskFiles.any { it.type == TaskFile.TaskFileType.POLYGON }
        val hasSolution = task.taskFiles.any { it.type == TaskFile.TaskFileType.SOLUTION }
        val isUsedInAnyContest = contestService.findAll().any { c -> c.tasks.any { it.id == task.id } }

        val attachedContests = contestService
            .findAll()
            .filter { c -> c.tasks.any { it.id == task.id } }
            .sortedBy { it.id }

        val lastTestStatus = when (task.testingStatus) {
            Task.TestingStatus.NOT_TESTED -> "Не запускалось"
            Task.TestingStatus.TESTING -> "В процессе"
            Task.TestingStatus.PASSED -> "Пройдено"
            Task.TestingStatus.FAILED -> "Не пройдено"
        }

        setupModel(model, session, developer)
        model.addAttribute("task", task)
        model.addAttribute("attachedTaskFiles", attachedItems)
        model.addAttribute("availableTaskFiles", availableItems)
        model.addAttribute("isTesting", task.testingStatus == Task.TestingStatus.TESTING)
        model.addAttribute("testReady", hasPolygon && hasSolution)
        model.addAttribute("numPolygons", task.taskFiles.count { it.type == TaskFile.TaskFileType.POLYGON })
        model.addAttribute("numSolutions", task.taskFiles.count { it.type == TaskFile.TaskFileType.SOLUTION })
        model.addAttribute("testStatus", lastTestStatus)
        model.addAttribute("isUsedInAnyContest", isUsedInAnyContest)
        model.addAttribute("attachedContests", attachedContests)

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

        val testSolutions = solutionService.findAll()
            .filter { it.task.id == task.id && it.contest == null }
            .sortedBy { it.id }

        setupModel(model, session, developer)
        model.addAttribute("task", task)
        model.addAttribute("solutions", testSolutions)

        return "developer/task-tests"
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
            return "redirect:/user/developer/tasks/${'$'}id"
        }

        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            redirectAttributes.addMessage("Название не может быть пустым.")
            return "redirect:/user/developer/tasks/${'$'}id"
        }

        task.name = trimmedName
        task.info = info?.takeIf { it.isNotBlank() }
        taskService.save(task)
        redirectAttributes.addMessage("Данные Задачи обновлены.")
        return "redirect:/user/developer/tasks/${'$'}id"
    }

    @PostMapping("/tasks/{id}/files/attach")
    fun attachTaskFile(
        @PathVariable id: Long,
        @RequestParam("taskFileId") taskFileId: Long,
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
            return "redirect:/user/developer/tasks/${'$'}id"
        }

        val tf = taskFileService.findById(taskFileId)
        if (tf == null) {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/tasks/${'$'}id"
        }
        if (tf.developer?.id != developer.id) {
            redirectAttributes.addMessage("Можно прикреплять только свои файлы.")
            return "redirect:/user/developer/tasks/${'$'}id"
        }

        if (task.testingStatus == Task.TestingStatus.TESTING) {
            redirectAttributes.addMessage("Нельзя изменять файлы во время тестирования.")
            return "redirect:/user/developer/tasks/${'$'}id"
        }

        val usedInContest = contestService.findAll().any { c -> c.tasks.any { it.id == task.id } }
        if (usedInContest) {
            redirectAttributes.addMessage("Нельзя изменять файлы Задачи, прикреплённой к Туру.")
            return "redirect:/user/developer/tasks/${'$'}id"
        }

        val added = task.taskFiles.add(tf)
        if (added && task.testingStatus == Task.TestingStatus.PASSED) {
            task.testingStatus = Task.TestingStatus.NOT_TESTED
        }
        taskService.save(task)
        if (added) {
            redirectAttributes.addMessage("Файл прикреплён к Задаче.")
        } else {
            redirectAttributes.addMessage("Файл уже был прикреплён.")
        }
        return "redirect:/user/developer/tasks/${'$'}id"
    }

    @PostMapping("/tasks/{taskId}/files/{fileId}/detach")
    fun detachTaskFile(
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
            return "redirect:/user/developer/tasks/${'$'}taskId"
        }

        if (task.testingStatus == Task.TestingStatus.TESTING) {
            redirectAttributes.addMessage("Нельзя изменять файлы во время тестирования.")
            return "redirect:/user/developer/tasks/${'$'}taskId"
        }

        val usedInContest = contestService.findAll().any { c -> c.tasks.any { it.id == task.id } }
        if (usedInContest) {
            redirectAttributes.addMessage("Нельзя изменять файлы Задачи, прикреплённой к Туру.")
            return "redirect:/user/developer/tasks/${'$'}taskId"
        }

        val removed = task.taskFiles.removeIf { it.id == fileId }
        if (removed && task.testingStatus == Task.TestingStatus.PASSED) {
            task.testingStatus = Task.TestingStatus.NOT_TESTED
        }
        taskService.save(task)
        if (removed) {
            redirectAttributes.addMessage("Файл откреплён от Задачи.")
        } else {
            redirectAttributes.addMessage("Файл не был прикреплён.")
        }
        return "redirect:/user/developer/tasks/${'$'}taskId"
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
            return "redirect:/user/developer/tasks/${'$'}id"
        }

        val usedInContest = contestService.findAll().any { c -> c.tasks.any { it.id == task.id } }
        if (usedInContest) {
            redirectAttributes.addMessage("Нельзя запускать тестирование для Задачи, прикреплённой к Туру.")
            return "redirect:/user/developer/tasks/${'$'}id"
        }

        if (task.testingStatus == Task.TestingStatus.TESTING) {
            redirectAttributes.addMessage("Задача уже тестируется.")
            return "redirect:/user/developer/tasks/${'$'}id"
        }

        val hasPolygon = task.taskFiles.any { it.type == TaskFile.TaskFileType.POLYGON }
        val hasSolution = task.taskFiles.any { it.type == TaskFile.TaskFileType.SOLUTION }
        if (!hasPolygon || !hasSolution) {
            redirectAttributes.addMessage("Для тестирования требуется минимум один Файл типа Полигон и один Файл типа Эталонное Решение.")
            return "redirect:/user/developer/tasks/${'$'}id"
        }

        // Placeholder: real integration with Grader should go here
        task.testingStatus = Task.TestingStatus.TESTING
        taskService.save(task)
        redirectAttributes.addMessage("Тестирование запущено.")
        return "redirect:/user/developer/tasks/${'$'}id"
    }

    // Task templates (kept here until a dedicated controller is introduced)
    @GetMapping("/task-templates")
    fun taskTemplatesPage(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        setupModel(model, session, developer)
        return "developer/task-templates"
    }

    @GetMapping("/task-templates/create")
    fun taskTemplateCreateForm(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        setupModel(model, session, developer)
        return "developer/task-template-create"
    }

    private data class TaskFileListItem(
        val id: Long,
        val name: String?,
        val info: String?,
        val createdAt: Instant?,
        val localizedType: String,
    )
}
