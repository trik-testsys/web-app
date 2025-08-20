package trik.testsys.webapp.backoffice.controller.impl.user

import jakarta.servlet.http.HttpSession
import org.springframework.web.multipart.MultipartFile
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
import trik.testsys.webapp.backoffice.data.entity.impl.Contest
import trik.testsys.webapp.backoffice.data.entity.impl.Task
import trik.testsys.webapp.backoffice.data.entity.impl.TaskFile
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.service.TaskService
import trik.testsys.webapp.backoffice.data.service.TaskFileService
import trik.testsys.webapp.backoffice.service.FileManager
import trik.testsys.webapp.backoffice.data.service.ContestService
import trik.testsys.webapp.backoffice.data.service.UserGroupService
import trik.testsys.webapp.backoffice.utils.addHasActiveSession
import trik.testsys.webapp.backoffice.utils.addMessage
import trik.testsys.webapp.backoffice.utils.addSections
import trik.testsys.webapp.backoffice.utils.addUser

@Controller
@RequestMapping("/user/developer")
class DeveloperController(
    private val contestService: ContestService,
    private val userGroupService: UserGroupService,
//    private val taskTemplateService: TaskTemplateService,
    private val taskService: TaskService,
    private val taskFileService: TaskFileService,
    private val fileManager: FileManager,
) : AbstractUserController() {

    @GetMapping("/tasks")
    fun tasksPage(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val createdTasks = taskService.findAll().filter { it.developer?.id == developer.id }.sortedBy { it.id }

        model.apply {
            addHasActiveSession(session)
            addUser(developer)
            addSections(menuBuilder.buildFor(developer))
            addAttribute("tasks", createdTasks)
        }

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

        model.apply {
            addHasActiveSession(session)
            addUser(developer)
            addSections(menuBuilder.buildFor(developer))
        }

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

        val lastTestStatus = when (task.testingStatus) {
            Task.TestingStatus.NOT_TESTED -> "Не запускалось"
            Task.TestingStatus.TESTING -> "В процессе"
            Task.TestingStatus.PASSED -> "Пройдено"
            Task.TestingStatus.FAILED -> "Не пройдено"
        }

        model.apply {
            addHasActiveSession(session)
            addUser(developer)
            addSections(menuBuilder.buildFor(developer))
            addAttribute("task", task)
            addAttribute("attachedTaskFiles", attachedItems)
            addAttribute("availableTaskFiles", availableItems)
            addAttribute("isTesting", task.testingStatus == Task.TestingStatus.TESTING)
            addAttribute("testReady", hasPolygon && hasSolution)
            addAttribute("numPolygons", task.taskFiles.count { it.type == TaskFile.TaskFileType.POLYGON })
            addAttribute("numSolutions", task.taskFiles.count { it.type == TaskFile.TaskFileType.SOLUTION })
            addAttribute("testStatus", lastTestStatus)
            addAttribute("isUsedInAnyContest", isUsedInAnyContest)
        }

        return "developer/task"
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
            return "redirect:/user/developer/tasks/$id"
        }

        val tf = taskFileService.findById(taskFileId)
        if (tf == null) {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/tasks/$id"
        }
        if (tf.developer?.id != developer.id) {
            redirectAttributes.addMessage("Можно прикреплять только свои файлы.")
            return "redirect:/user/developer/tasks/$id"
        }

        if (task.testingStatus == Task.TestingStatus.TESTING) {
            redirectAttributes.addMessage("Нельзя изменять файлы во время тестирования.")
            return "redirect:/user/developer/tasks/$id"
        }

        val usedInContest = contestService.findAll().any { c -> c.tasks.any { it.id == task.id } }
        if (usedInContest) {
            redirectAttributes.addMessage("Нельзя изменять файлы Задачи, прикреплённой к Туру.")
            return "redirect:/user/developer/tasks/$id"
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
        return "redirect:/user/developer/tasks/$id"
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
            return "redirect:/user/developer/tasks/$taskId"
        }

        if (task.testingStatus == Task.TestingStatus.TESTING) {
            redirectAttributes.addMessage("Нельзя изменять файлы во время тестирования.")
            return "redirect:/user/developer/tasks/$taskId"
        }

        val usedInContest = contestService.findAll().any { c -> c.tasks.any { it.id == task.id } }
        if (usedInContest) {
            redirectAttributes.addMessage("Нельзя изменять файлы Задачи, прикреплённой к Туру.")
            return "redirect:/user/developer/tasks/$taskId"
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

        val hasPolygon = task.taskFiles.any { it.type == TaskFile.TaskFileType.POLYGON }
        val hasSolution = task.taskFiles.any { it.type == TaskFile.TaskFileType.SOLUTION }
        if (!hasPolygon || !hasSolution) {
            redirectAttributes.addMessage("Для тестирования требуется минимум один Файл типа Полигон и один Файл типа Эталонное Решение.")
            return "redirect:/user/developer/tasks/$id"
        }

        // Placeholder: real integration with Grader should go here
        task.testingStatus = Task.TestingStatus.TESTING
        taskService.save(task)
        redirectAttributes.addMessage("Тестирование запущено.")
        return "redirect:/user/developer/tasks/$id"
    }

    @GetMapping("/task-templates")
    fun taskTemplatesPage(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

//        val ownedTemplates = taskTemplateService.findByDeveloper(developer).sortedBy { it.id }
//        val availableTemplates = taskTemplateService
//            .findForUserGroups(developer.memberedGroups)
//            .sortedBy { it.id }

        model.apply {
            addHasActiveSession(session)
            addUser(developer)
            addSections(menuBuilder.buildFor(developer))
//            addAttribute("ownedTemplates", ownedTemplates)
//            addAttribute("availableTemplates", availableTemplates)
        }

        return "developer/task-templates"
    }

    @GetMapping("/task-files")
    fun taskFilesPage(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val allTaskFiles = taskFileService.findByDeveloper(developer).sortedBy { it.id }

        fun toListItem(tf: TaskFile) = TaskFileListItem(
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

        val polygonFiles = allTaskFiles.filter { it.type == TaskFile.TaskFileType.POLYGON }.map(::toListItem)
        val exerciseFiles = allTaskFiles.filter { it.type == TaskFile.TaskFileType.EXERCISE }.map(::toListItem)
        val solutionFiles = allTaskFiles.filter { it.type == TaskFile.TaskFileType.SOLUTION }.map(::toListItem)
        val conditionFiles = allTaskFiles.filter { it.type == TaskFile.TaskFileType.CONDITION }.map(::toListItem)

        model.apply {
            addHasActiveSession(session)
            addUser(developer)
            addSections(menuBuilder.buildFor(developer))
            addAttribute("polygonFiles", polygonFiles)
            addAttribute("exerciseFiles", exerciseFiles)
            addAttribute("solutionFiles", solutionFiles)
            addAttribute("conditionFiles", conditionFiles)
        }

        return "developer/task-files"
    }

    @GetMapping("/task-files/create")
    fun taskFileCreateForm(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        model.apply {
            addHasActiveSession(session)
            addUser(developer)
            addSections(menuBuilder.buildFor(developer))
        }

        return "developer/task-file-create"
    }

    @PostMapping("/task-files/create")
    fun createTaskFile(
        @RequestParam name: String,
        @RequestParam(required = false) info: String?,
        @RequestParam type: String,
        @RequestParam("file") file: MultipartFile,
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
            return "redirect:/user/developer/task-files"
        }

        val tfType = try {
            TaskFile.TaskFileType.valueOf(type)
        } catch (e: Exception) {
            redirectAttributes.addMessage("Некорректный тип файла.")
            return "redirect:/user/developer/task-files"
        }

        val taskFile = TaskFile().also {
            it.name = trimmedName
            it.developer = developer
            it.info = info?.takeIf { s -> s.isNotBlank() }
            it.type = tfType
        }

        val saved = fileManager.saveTaskFile(taskFile, file)
        if (!saved) {
            taskFileService.delete(taskFile)
            redirectAttributes.addMessage("Не удалось сохранить файл на диск.")
            return "redirect:/user/developer/task-files"
        }

        redirectAttributes.addMessage("Файл создан (id=${'$'}{taskFile.id}).")
        return "redirect:/user/developer/task-files"
    }

    @GetMapping("/task-files/{id}")
    fun viewTaskFile(
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

        val tf = taskFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }
        if (tf.developer?.id != developer.id) {
            redirectAttributes.addMessage("У вас нет доступа к этому Файлу.")
            return "redirect:/user/developer/task-files"
        }

        val localizedType = when (tf.type) {
            TaskFile.TaskFileType.POLYGON -> "Полигон"
            TaskFile.TaskFileType.EXERCISE -> "Упражнение"
            TaskFile.TaskFileType.SOLUTION -> "Эталонное Решение"
            TaskFile.TaskFileType.CONDITION -> "Условие"
            else -> tf.type?.name ?: "—"
        }

        val versions = fileManager.listTaskFileVersions(tf)

        model.apply {
            addHasActiveSession(session)
            addUser(developer)
            addSections(menuBuilder.buildFor(developer))
            addAttribute("taskFile", tf)
            addAttribute("localizedType", localizedType)
            addAttribute("versions", versions)
        }

        return "developer/task-file"
    }

    @PostMapping("/task-files/{id}/upload")
    fun updateTaskFile(
        @PathVariable id: Long,
        @RequestParam("file") file: MultipartFile,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val tf = taskFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }
        if (tf.developer?.id != developer.id) {
            redirectAttributes.addMessage("Редактирование доступно только владельцу.")
            return "redirect:/user/developer/task-files/$id"
        }

        tf.fileVersion = (tf.fileVersion + 1)
        val saved = fileManager.saveTaskFile(tf, file)
        if (!saved) {
            tf.fileVersion = (tf.fileVersion - 1)
            redirectAttributes.addMessage("Не удалось сохранить файл.")
            return "redirect:/user/developer/task-files/$id"
        }

        taskFileService.save(tf)
        redirectAttributes.addMessage("Файл обновлён.")
        return "redirect:/user/developer/task-files/$id"
    }

    @GetMapping("/task-files/{id}/download/{version}")
    fun downloadTaskFileVersion(
        @PathVariable id: Long,
        @PathVariable version: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): Any {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val tf = taskFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }
        if (tf.developer?.id != developer.id) {
            redirectAttributes.addMessage("У вас нет доступа к этому Файлу.")
            return "redirect:/user/developer/task-files"
        }

        val file = fileManager.getTaskFileVersion(tf, version) ?: run {
            redirectAttributes.addMessage("Версия не найдена.")
            return "redirect:/user/developer/task-files/$id"
        }

        val bytes = file.readBytes()
        val disposition = "attachment; filename=\"${file.name}\""
        return org.springframework.http.ResponseEntity.ok()
            .header("Content-Disposition", disposition)
            .header("Content-Type", org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .header("Content-Length", bytes.size.toString())
            .body(bytes)
    }

    private data class TaskFileListItem(
        val id: Long,
        val name: String?,
        val info: String?,
        val createdAt: Instant?,
        val localizedType: String,
    )

    @GetMapping("/task-templates/create")
    fun taskTemplateCreateForm(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        model.apply {
            addHasActiveSession(session)
            addUser(developer)
            addSections(menuBuilder.buildFor(developer))
        }

        return "developer/task-template-create"
    }

//    @PostMapping("/task-templates/create")
//    fun createTaskTemplate(
//        @RequestParam name: String,
//        @RequestParam(required = false) info: String?,
//        session: HttpSession,
//        redirectAttributes: RedirectAttributes
//    ): String {
//        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
//        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"
//
//        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
//            redirectAttributes.addMessage("Недостаточно прав.")
//            return "redirect:/user"
//        }
//
//        val trimmedName = name.trim()
//        if (trimmedName.isEmpty()) {
//            redirectAttributes.addMessage("Название не может быть пустым.")
//            return "redirect:/user/developer/task-templates"
//        }
//
//        val template = TaskTemplate().also {
//            it.name = trimmedName
//            it.developer = developer
//            it.info = info?.takeIf { s -> s.isNotBlank() }
//        }
//        taskTemplateService.save(template)
//        redirectAttributes.addMessage("Шаблон создан (id=${template.id}).")
//        return "redirect:/user/developer/task-templates"
//    }

//    @GetMapping("/task-templates/{id}")
//    fun viewTaskTemplate(
//        @PathVariable id: Long,
//        model: Model,
//        session: HttpSession,
//        redirectAttributes: RedirectAttributes
//    ): String {
//        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
//        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"
//
//        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
//            redirectAttributes.addMessage("Недостаточно прав.")
//            return "redirect:/user"
//        }
//
//        val template = taskTemplateService.findById(id) ?: run {
//            redirectAttributes.addMessage("Шаблон не найден.")
//            return "redirect:/user/developer/task-templates"
//        }
//
//        val isOwner = template.developer?.id == developer.id
//        if (!isOwner) {
//            val canView = developer.memberedGroups.any { mg -> template.userGroups.any { ug -> ug.id == mg.id } }
//            if (!canView) {
//                redirectAttributes.addMessage("У вас нет доступа к этому Шаблону.")
//                return "redirect:/user/developer/task-templates"
//            }
//        }
//
//        model.apply {
//            addHasActiveSession(session)
//            addUser(developer)
//            addSections(menuBuilder.buildFor(developer))
//            addAttribute("template", template)
//            addAttribute(
//                "availableUserGroups",
//                userGroupService.findByMember(developer)
//                    .filterNot { g -> template.userGroups.any { it.id == g.id } }
//                    .sortedBy { it.id }
//            )
//            addAttribute("isOwner", isOwner)
//        }
//
//        return "developer/task-template"
//    }

//    @PostMapping("/task-templates/{id}/update")
//    fun updateTaskTemplate(
//        @PathVariable id: Long,
//        @RequestParam name: String,
//        @RequestParam(required = false) info: String?,
//        session: HttpSession,
//        redirectAttributes: RedirectAttributes
//    ): String {
//        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
//        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"
//
//        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
//            redirectAttributes.addMessage("Недостаточно прав.")
//            return "redirect:/user"
//        }
//
//        val template = taskTemplateService.findById(id) ?: run {
//            redirectAttributes.addMessage("Шаблон не найден.")
//            return "redirect:/user/developer/task-templates"
//        }
//        if (template.developer?.id != developer.id) {
//            redirectAttributes.addMessage("Редактирование доступно только владельцу.")
//            return "redirect:/user/developer/task-templates/$id"
//        }
//
//        val trimmedName = name.trim()
//        if (trimmedName.isEmpty()) {
//            redirectAttributes.addMessage("Название не может быть пустым.")
//            return "redirect:/user/developer/task-templates/$id"
//        }
//
//        template.name = trimmedName
//        template.info = info?.takeIf { it.isNotBlank() }
//        taskTemplateService.save(template)
//        redirectAttributes.addMessage("Данные Шаблона обновлены.")
//        return "redirect:/user/developer/task-templates/$id"
//    }

//    @PostMapping("/task-templates/{id}/groups/add")
//    fun addTaskTemplateGroup(
//        @PathVariable id: Long,
//        @RequestParam("groupId") groupId: Long,
//        session: HttpSession,
//        redirectAttributes: RedirectAttributes
//    ): String {
//        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
//        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"
//
//        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
//            redirectAttributes.addMessage("Недостаточно прав.")
//            return "redirect:/user"
//        }
//
//        val template = taskTemplateService.findById(id) ?: run {
//            redirectAttributes.addMessage("Шаблон не найден.")
//            return "redirect:/user/developer/task-templates"
//        }
//        if (template.developer?.id != developer.id) {
//            redirectAttributes.addMessage("Изменение доступов доступно только владельцу.")
//            return "redirect:/user/developer/task-templates/$id"
//        }
//
//        val group = userGroupService.findById(groupId)
//        if (group == null) {
//            redirectAttributes.addMessage("Группа не найдена.")
//            return "redirect:/user/developer/task-templates/$id"
//        }
//
//        val memberGroups = userGroupService.findByMember(developer)
//        if (memberGroups.none { it.id == group.id }) {
//            redirectAttributes.addMessage("Можно добавлять только группы, в которых вы состоите.")
//            return "redirect:/user/developer/task-templates/$id"
//        }
//
//        val added = template.userGroups.add(group)
//        taskTemplateService.save(template)
//        if (added) {
//            redirectAttributes.addMessage("Группа добавлена к Шаблону.")
//        } else {
//            redirectAttributes.addMessage("Группа уже была добавлена.")
//        }
//        return "redirect:/user/developer/task-templates/$id"
//    }

    @GetMapping("/contests/{id}")
    fun view(
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

        val contest = contestService.findById(id) ?: run {
            redirectAttributes.addMessage("Тур не найден.")
            return "redirect:/user/developer/contests"
        }

        val isOwner = contest.developer?.id == developer.id
        if (!isOwner) {
            val canView = developer.memberedGroups.any { mg -> contest.userGroups.any { ug -> ug.id == mg.id } }
            if (!canView) {
                redirectAttributes.addMessage("У вас нет доступа к этому Туру.")
                return "redirect:/user/developer/contests"
            }
        }

        model.apply {
            addHasActiveSession(session)
            addUser(developer)
            addSections(menuBuilder.buildFor(developer))
            addAttribute("contest", contest)
            addAttribute("isOwner", isOwner)
            addAttribute(
                "availableUserGroups",
                userGroupService.findByMember(developer)
                    .filterNot { g -> contest.userGroups.any { it.id == g.id } }
                    .sortedBy { it.id }
            )
            addAttribute("attachedTasks", contest.tasks.sortedBy { it.id })
            addAttribute(
                "availableTasks",
                taskService.findAll().filter { it.developer?.id == developer.id }
                    .filterNot { t -> contest.tasks.any { it.id == t.id } }
                    .sortedBy { it.id }
            )
        }

        return "developer/contest"
    }

    @PostMapping("/contests/{id}/tasks/attach")
    fun attachTaskToContest(
        @PathVariable id: Long,
        @RequestParam("taskId") taskId: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val contest = contestService.findById(id) ?: run {
            redirectAttributes.addMessage("Тур не найден.")
            return "redirect:/user/developer/contests"
        }
        if (contest.developer?.id != developer.id) {
            redirectAttributes.addMessage("Доступно только владельцу.")
            return "redirect:/user/developer/contests/$id"
        }

        val task = taskService.findById(taskId)
        if (task == null || task.developer?.id != developer.id) {
            redirectAttributes.addMessage("Задача не найдена или недоступна.")
            return "redirect:/user/developer/contests/$id"
        }

        if (task.testingStatus != Task.TestingStatus.PASSED) {
            redirectAttributes.addMessage("Можно прикреплять только Задачи со статусом тестирования Пройдено.")
            return "redirect:/user/developer/contests/$id"
        }

        val added = contest.tasks.add(task)
        contestService.save(contest)
        if (added) redirectAttributes.addMessage("Задача прикреплена к Туру.") else redirectAttributes.addMessage("Задача уже была прикреплена.")
        return "redirect:/user/developer/contests/$id"
    }

    @PostMapping("/contests/{contestId}/tasks/{taskId}/detach")
    fun detachTaskFromContest(
        @PathVariable contestId: Long,
        @PathVariable taskId: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val contest = contestService.findById(contestId) ?: run {
            redirectAttributes.addMessage("Тур не найден.")
            return "redirect:/user/developer/contests"
        }
        if (contest.developer?.id != developer.id) {
            redirectAttributes.addMessage("Доступно только владельцу.")
            return "redirect:/user/developer/contests/$contestId"
        }

        val removed = contest.tasks.removeIf { it.id == taskId }
        contestService.save(contest)
        if (removed) redirectAttributes.addMessage("Задача откреплена от Тура.") else redirectAttributes.addMessage("Задача не была прикреплена.")
        return "redirect:/user/developer/contests/$contestId"
    }

    @PostMapping("/contests/{id}/rename")
    fun rename(
        @PathVariable id: Long,
        @RequestParam name: String,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val contest = contestService.findById(id) ?: run {
            redirectAttributes.addMessage("Тур не найден.")
            return "redirect:/user/developer/contests"
        }
        if (contest.developer?.id != developer.id) {
            redirectAttributes.addMessage("Редактирование доступно только владельцу.")
            return "redirect:/user/developer/contests/$id"
        }

        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            redirectAttributes.addMessage("Название не может быть пустым.")
            return "redirect:/user/developer/contests/$id"
        }

        contest.name = trimmed
        contestService.save(contest)
        redirectAttributes.addMessage("Название Тура обновлено.")
        return "redirect:/user/developer/contests/$id"
    }

    @PostMapping("/contests/{id}/update")
    fun update(
        @PathVariable id: Long,
        @RequestParam name: String,
        @RequestParam(required = false) info: String?,
        @RequestParam startsAt: String,
        @RequestParam(required = false) endsAt: String?,
        @RequestParam(required = false) duration: Long?,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val contest = contestService.findById(id) ?: run {
            redirectAttributes.addMessage("Тур не найден.")
            return "redirect:/user/developer/contests"
        }
        if (contest.developer?.id != developer.id) {
            redirectAttributes.addMessage("Редактирование доступно только владельцу.")
            return "redirect:/user/developer/contests/$id"
        }

        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            redirectAttributes.addMessage("Название не может быть пустым.")
            return "redirect:/user/developer/contests/$id"
        }

        if (duration != null && duration <= 0) {
            redirectAttributes.addMessage("Время на прохождение должно быть положительной.")
            return "redirect:/user/developer/contests/$id"
        }

        val startsInstant = try {
            val ldt = java.time.LocalDateTime.parse(startsAt)
            ldt.atZone(java.time.ZoneId.systemDefault()).toInstant()
        } catch (e: Exception) {
            redirectAttributes.addMessage("Некорректная дата начала. Формат: yyyy-MM-dd'T'HH:mm")
            return "redirect:/user/developer/contests/$id"
        }

        val endsInstant = if (!endsAt.isNullOrBlank()) {
            try {
                val ldt = java.time.LocalDateTime.parse(endsAt)
                ldt.atZone(java.time.ZoneId.systemDefault()).toInstant()
            } catch (e: Exception) {
                redirectAttributes.addMessage("Некорректная дата окончания. Формат: yyyy-MM-dd'T'HH:mm")
                return "redirect:/user/developer/contests/$id"
            }
        } else null

        if (endsInstant != null && endsInstant.isBefore(startsInstant)) {
            redirectAttributes.addMessage("Окончание не может быть раньше начала.")
            return "redirect:/user/developer/contests/$id"
        }

        contest.name = trimmedName
        contest.info = info?.takeIf { it.isNotBlank() }
        contest.startsAt = startsInstant
        contest.endsAt = endsInstant
        contest.duration = duration
        contestService.save(contest)
        redirectAttributes.addMessage("Данные Тура обновлены.")
        return "redirect:/user/developer/contests/$id"
    }

    @GetMapping("/contests/create")
    fun createForm(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        model.apply {
            addHasActiveSession(session)
            addUser(developer)
            addSections(menuBuilder.buildFor(developer))
        }
        return "developer/contest-create"
    }

    @GetMapping("/contests")
    fun contestsPage(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val createdContests = contestService.findAll().filter { it.developer?.id == developer.id }.sortedBy { it.id }
        val sharedContests = contestService.findForUser(developer).sortedBy { it.id }

        model.apply {
            addHasActiveSession(session)
            addUser(developer)
            addSections(menuBuilder.buildFor(developer))
            addAttribute("createdContests", createdContests)
            addAttribute("sharedContests", sharedContests)
        }

        return "developer/contests"
    }

    @PostMapping("/contests/create")
    fun createContest(
        @RequestParam name: String,
        @RequestParam(required = false) info: String?,
        @RequestParam startsAt: String,
        @RequestParam(required = false) endsAt: String?,
        @RequestParam(required = false) duration: Long?,
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
            return "redirect:/user/developer/contests"
        }

        if (duration != null && duration <= 0) {
            redirectAttributes.addMessage("Время на прохождение должно быть положительной.")
            return "redirect:/user/developer/contests"
        }

        val startsInstant = try {
            val ldt = java.time.LocalDateTime.parse(startsAt)
            ldt.atZone(java.time.ZoneId.systemDefault()).toInstant()
        } catch (e: Exception) {
            redirectAttributes.addMessage("Некорректная дата начала. Формат: yyyy-MM-dd'T'HH:mm")
            return "redirect:/user/developer/contests"
        }

        val endsInstant = if (!endsAt.isNullOrBlank()) {
            try {
                val ldt = java.time.LocalDateTime.parse(endsAt)
                ldt.atZone(java.time.ZoneId.systemDefault()).toInstant()
            } catch (e: Exception) {
                redirectAttributes.addMessage("Некорректная дата окончания. Формат: yyyy-MM-dd'T'HH:mm")
                return "redirect:/user/developer/contests"
            }
        } else null

        if (endsInstant != null && endsInstant.isBefore(startsInstant)) {
            redirectAttributes.addMessage("Окончание не может быть раньше начала.")
            return "redirect:/user/developer/contests"
        }

        val contest = Contest().also {
            it.name = trimmedName
            it.info = info?.takeIf { s -> s.isNotBlank() }
            it.startsAt = startsInstant
            it.endsAt = endsInstant
            it.duration = duration
            it.developer = developer
        }
        contestService.save(contest)
        redirectAttributes.addMessage("Тур создан (id=${contest.id}).")
        return "redirect:/user/developer/contests"
    }

    @PostMapping("/contests/{id}/groups/add")
    fun addContestGroup(
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

        val contest = contestService.findById(id) ?: run {
            redirectAttributes.addMessage("Тур не найден.")
            return "redirect:/user/developer/contests"
        }
        if (contest.developer?.id != developer.id) {
            redirectAttributes.addMessage("Изменение доступов доступно только владельцу.")
            return "redirect:/user/developer/contests/$id"
        }

        val group = userGroupService.findById(groupId)
        if (group == null) {
            redirectAttributes.addMessage("Группа не найдена.")
            return "redirect:/user/developer/contests/$id"
        }

        val memberGroups = userGroupService.findByMember(developer)
        if (memberGroups.none { it.id == group.id }) {
            redirectAttributes.addMessage("Можно добавлять только группы, в которых вы состоите.")
            return "redirect:/user/developer/contests/$id"
        }

        val added = contest.userGroups.add(group)
        contestService.save(contest)
        if (added) {
            redirectAttributes.addMessage("Группа добавлена к Турy.")
        } else {
            redirectAttributes.addMessage("Группа уже была добавлена.")
        }
        return "redirect:/user/developer/contests/$id"
    }
}