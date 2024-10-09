package trik.testsys.webclient.controller.impl.user.developer

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.impl.user.developer.DeveloperMainController.Companion.DEVELOPER_PAGE
import trik.testsys.webclient.controller.impl.user.developer.DeveloperTaskFileController.Companion.checkTaskFileExistence
import trik.testsys.webclient.controller.impl.user.developer.DeveloperTasksController.Companion.TASKS_PATH
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.controller.user.AbstractWebUserMainController.Companion.LOGIN_PATH
import trik.testsys.webclient.entity.impl.Solution
import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.entity.user.impl.Developer
import trik.testsys.webclient.service.entity.impl.SolutionService
import trik.testsys.webclient.service.entity.impl.TaskFileService
import trik.testsys.webclient.service.entity.impl.TaskService
import trik.testsys.webclient.service.entity.user.impl.DeveloperService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.addPopupMessage
import trik.testsys.webclient.view.impl.DeveloperView
import trik.testsys.webclient.view.impl.TaskCreationView
import trik.testsys.webclient.view.impl.TaskFileView.Companion.toView
import trik.testsys.webclient.view.impl.TaskView
import trik.testsys.webclient.view.impl.TaskView.Companion.toView
import java.util.*
import javax.servlet.http.HttpServletRequest

@Controller
@RequestMapping(DeveloperTaskController.TASK_PATH)
class DeveloperTaskController(
    loginData: LoginData,

    private val taskService: TaskService,
    private val taskFileService: TaskFileService,
    private val solutionService: SolutionService
) : AbstractWebUserController<Developer, DeveloperView, DeveloperService>(loginData) {

    override val mainPage = TASK_PAGE

    override val mainPath = TASK_PATH

    override fun Developer.toView(timeZone: TimeZone) = TODO()

    @PostMapping("/create")
    fun taskPost(
        @ModelAttribute("task") taskView: TaskCreationView,
        timeZone: TimeZone,
        request: HttpServletRequest,
        redirectAttributes: RedirectAttributes
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        val task = taskView.toEntity(webUser)

        taskService.validate(task, redirectAttributes, "redirect:$TASKS_PATH")?.let { return it }

        taskService.save(task)

        redirectAttributes.addPopupMessage("Задание '${task.name}' успешно создана.")

        return "redirect:$TASKS_PATH"
    }

    @GetMapping("/{taskId}")
    fun taskGet(
        @PathVariable("taskId") taskId: Long,
        timeZone: TimeZone,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        if (!webUser.checkTaskExistence(taskId)) {
            redirectAttributes.addPopupMessage("Задание с ID $taskId не найдено.")
            return "redirect:$TASKS_PATH"
        }

        val task = taskService.find(taskId) ?: run {
            redirectAttributes.addPopupMessage("Задание с ID $taskId не найдено.")
            return "redirect:$TASKS_PATH"
        }

        val taskView = task.toView(timeZone)
        model.addAttribute(TASK_ATTR, taskView)

        val taskFiles = webUser.taskFiles.map { it.toView(timeZone) }.sortedBy { it.id }
        model.addAttribute(TASK_FILES_ATTR, taskFiles)

        return TASK_PAGE
    }

    @PostMapping("/update/{taskId}")
    fun taskUpdate(
        @PathVariable("taskId") taskId: Long,
        @ModelAttribute("task") taskView: TaskView,
        timeZone: TimeZone,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        if (!webUser.checkTaskExistence(taskId)) {
            redirectAttributes.addPopupMessage("Задание с ID $taskId не найдено.")
            return "redirect:$TASKS_PATH"
        }

        val task = taskView.toEntity(timeZone)
        task.developer = webUser

        taskService.validate(task, redirectAttributes, "redirect:$TASK_PATH/$taskId")?.let { return it }

        val updatedTask = taskService.save(task)

        model.addAttribute(TASK_ATTR, updatedTask.toView(timeZone))
        redirectAttributes.addPopupMessage("Данные успешно изменены.")

        return "redirect:$TASK_PATH/$taskId"
    }

    @PostMapping("/attachTaskFile/{taskId}")
    fun attachTaskFileToTask(
        @PathVariable("taskId") taskId: Long,
        @RequestParam("taskFileId") taskFileId: Long,
        timeZone: TimeZone,
        redirectAttributes: RedirectAttributes
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        val task = taskService.find(taskId) ?: run {
            redirectAttributes.addPopupMessage("Задание с ID $taskId не найдено.")
            return "redirect:$TASKS_PATH"
        }

        if (!webUser.checkTaskExistence(taskId)) {
            redirectAttributes.addPopupMessage("Задание с ID $taskId не найдено.")
            return "redirect:$TASKS_PATH"
        }

        val taskFile = taskFileService.find(taskFileId) ?: run {
            redirectAttributes.addPopupMessage("Файл с ID $taskFileId не найден.")
            return "redirect:$TASK_PATH/$taskId"
        }

        if (!webUser.checkTaskFileExistence(taskFileId)) {
            redirectAttributes.addPopupMessage("Файл с ID $taskFileId не найден.")
            return "redirect:$TASK_PATH/$taskId"
        }

        task.taskFiles.add(taskFile)
        taskService.save(task)

        taskFile.tasks.add(task)
        taskFileService.save(taskFile)

        redirectAttributes.addPopupMessage("Файл ${taskFile.name} успешно прикреплен к заданию ${task.name}.")

        return "redirect:$TASK_PATH/$taskId"
    }

    @PostMapping("/test/{taskId}")
    fun testTask(
        @PathVariable("taskId") taskId: Long,
        timeZone: TimeZone,
        redirectAttributes: RedirectAttributes
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        val task = taskService.find(taskId) ?: run {
            redirectAttributes.addPopupMessage("Задание с ID $taskId не найдено.")
            return "redirect:$TASKS_PATH"
        }

        if (!webUser.checkTaskExistence(taskId)) {
            redirectAttributes.addPopupMessage("Задание с ID $taskId не найдено.")
            return "redirect:$TASKS_PATH"
        }

        val solution = Solution().also {
            it.task = task
            it.developer = webUser
        }
        solutionService.save(solution)

//        grader.sendToGrade(solution, task, Grader.GradingOptions(true, "1.0.0"))

        redirectAttributes.addPopupMessage("Тестирование задания ${task.name} запущено.")

        return "redirect:$TASK_PATH/$taskId"
    }

    companion object {

        const val TASK_PATH = "$TASKS_PATH/task"
        const val TASK_PAGE = "$DEVELOPER_PAGE/task"

        const val TASK_ATTR = "task"

        const val TASK_FILES_ATTR = "taskFiles"

        fun Developer.checkTaskExistence(taskId: Long?) = tasks.any { it.id == taskId }

        fun TaskService.validate(task: Task, redirectAttributes: RedirectAttributes, redirect: String): String? {
            if (!validateName(task)) {
                redirectAttributes.addPopupMessage("Название Задания не должно содержать Код-доступа.")
                return redirect
            }

            if (!validateAdditionalInfo(task)) {
                redirectAttributes.addPopupMessage("Дополнительная информация не должна содержать Код-доступа.")
                return redirect
            }

            return null
        }
    }
}