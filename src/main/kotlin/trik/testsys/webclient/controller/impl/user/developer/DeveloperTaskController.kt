package trik.testsys.webclient.controller.impl.user.developer

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
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
import trik.testsys.webclient.entity.impl.TaskFile
import trik.testsys.webclient.entity.user.impl.Developer
import trik.testsys.webclient.service.FileManager
import trik.testsys.webclient.service.Grader
import trik.testsys.webclient.service.entity.impl.ContestService
import trik.testsys.webclient.service.entity.impl.SolutionService
import trik.testsys.webclient.service.entity.impl.TaskFileService
import trik.testsys.webclient.service.entity.impl.TaskService
import trik.testsys.webclient.service.entity.user.impl.DeveloperService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.addPopupMessage
import trik.testsys.webclient.view.impl.DeveloperView
import trik.testsys.webclient.view.impl.TaskCreationView
import trik.testsys.webclient.view.impl.TaskFileView.Companion.toView
import trik.testsys.webclient.view.impl.TaskTestResultView.Companion.toTaskTestResultView
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
    private val solutionService: SolutionService,
    private val contestService: ContestService,

    private val grader: Grader,
    private val fileManager: FileManager
) : AbstractWebUserController<Developer, DeveloperView, DeveloperService>(loginData) {

    override val mainPage = TASK_PAGE

    override val mainPath = TASK_PATH

    override fun Developer.toView(timeZoneId: String?) = TODO()

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
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
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

        val taskView = task.toView(timezone)
        model.addAttribute(TASK_ATTR, taskView)

        val taskFiles = webUser.taskFiles
            .filter { it !in task.taskFiles }
            .map { it.toView(timezone) }
            .sortedBy { it.id }
        model.addAttribute(TASK_FILES_ATTR, taskFiles)

        val taskTests = solutionService.findTaskTests(task)
        model.addAttribute(TEST_RESULTS, taskTests.sortedByDescending { it.creationDate }.map { it.toTaskTestResultView(timezone) })

        return TASK_PAGE
    }

    @PostMapping("/update/{taskId}")
    fun taskUpdate(
        @PathVariable("taskId") taskId: Long,
        @ModelAttribute("task") taskView: TaskView,
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        if (!webUser.checkTaskExistence(taskId)) {
            redirectAttributes.addPopupMessage("Задание с ID $taskId не найдено.")
            return "redirect:$TASKS_PATH"
        }

        val prevTask = taskService.find(taskId) ?: run {
            redirectAttributes.addPopupMessage("Задание с ID $taskId не найдено.")
            return "redirect:$TASKS_PATH"
        }

        val task = taskView.toEntity(timezone).also {
            if (prevTask.passedTests) {
                it.pass()
            } else {
                it.fail()
            }

            it.developer = webUser
        }

        taskService.validate(task, redirectAttributes, "redirect:$TASK_PATH/$taskId")?.let { return it }

        val updatedTask = taskService.save(task)

        model.addAttribute(TASK_ATTR, updatedTask.toView(timezone))
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

        if (taskFile.type == TaskFile.TaskFileType.SOLUTION && task.hasSolution) {
            redirectAttributes.addPopupMessage("Задание ${task.name} уже имеет Эталонное решение.")
            return "redirect:$TASK_PATH/$taskId"
        }

        if (taskFile.type == TaskFile.TaskFileType.EXERCISE && task.hasExercise) {
            redirectAttributes.addPopupMessage("Задание ${task.name} уже имеет Упражнение.")
            return "redirect:$TASK_PATH/$taskId"
        }

        if (taskFile.type == TaskFile.TaskFileType.CONDITION && task.hasCondition) {
            redirectAttributes.addPopupMessage("Задание ${task.name} уже имеет Условие.")
            return "redirect:$TASK_PATH/$taskId"
        }

        if (taskFile.type.cannotBeRemovedOnTaskTesting()) {
            task.contests.forEach {
                it.tasks.remove(task)
                contestService.save(it)
            }
            task.contests.clear()

            task.fail()
        }

        taskFile.tasks.add(task)
        taskFileService.save(taskFile)

        task.taskFiles.add(taskFile)
        taskService.save(task)

        redirectAttributes.addPopupMessage("Файл ${taskFile.name} успешно прикреплен к заданию ${task.name}.")

        return "redirect:$TASK_PATH/$taskId"
    }

    @PostMapping("/deAttachTaskFile/{taskId}")
    fun deAttachTaskFileToTask(
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

        val taskTest = solutionService.findTaskTests(task)
        val isTaskTestingNow =  taskTest.any { it.status == Solution.SolutionStatus.IN_PROGRESS }

        if (isTaskTestingNow && taskFile.type.cannotBeRemovedOnTaskTesting()) {
            redirectAttributes.addPopupMessage("Тестирование Задания ${task.name} в процессе. Открепление Файла невозможно.")
            return "redirect:$TASK_PATH/$taskId"
        }

        if (taskFile.type.cannotBeRemovedOnTaskTesting()) {
            task.contests.forEach {
                it.tasks.remove(task)
                contestService.save(it)
            }
            task.contests.clear()

            task.fail()
        }

        taskFile.tasks.remove(task)
        taskFileService.save(taskFile)

        task.taskFiles.remove(taskFile)
        taskService.save(task)

        redirectAttributes.addPopupMessage("Файл ${taskFile.name} успешно откреплен от задания ${task.name}.")

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

        if (!task.hasSolution) {
            redirectAttributes.addPopupMessage("Задание ${task.name} не имеет Эталонного решения. Тестирование невозможно.")
            return "redirect:$TASK_PATH/$taskId"
        }

        if (task.polygonsCount == 0L) {
            redirectAttributes.addPopupMessage("Задание ${task.name} не имеет Полигонов. Тестирование невозможно.")
            return "redirect:$TASK_PATH/$taskId"
        }

        if (!task.hasExercise) {
            redirectAttributes.addPopupMessage("Задание ${task.name} не имеет Упражнение. Тестирование невозможно.")
            return "redirect:$TASK_PATH/$taskId"
        }

        val solutionFile = fileManager.getTaskFile(task.solution!!)
        val solution = Solution.qrsSolution(task)
        solutionService.save(solution)

        fileManager.saveSolutionFile(solution, solutionFile!!)

        grader.sendToGrade(
            solution,
            Grader.GradingOptions(true, "testsystrik/trik-studio:release-2023.1-2024-10-10-2.0.0")
        )

        redirectAttributes.addPopupMessage("Тестирование задания ${task.name} запущено.")

        return "redirect:$TASK_PATH/$taskId"
    }

    @GetMapping("/downloadRecording/{taskId}")
    fun getTaskTestingRecording(
        @PathVariable("taskId") taskId: Long,
        @RequestParam("solutionId") solutionId: Long,
        redirectAttributes: RedirectAttributes
    ): Any {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        if (!webUser.checkTaskExistence(taskId)) {
            redirectAttributes.addPopupMessage("Задание с ID $taskId не найдено.")
            return "redirect:$TASKS_PATH"
        }

        val task = taskService.find(taskId) ?: run {
            redirectAttributes.addPopupMessage("Задание с ID $taskId не найдено.")
            return "redirect:$TASKS_PATH"
        }

        val solution = solutionService.find(solutionId) ?: run {
            redirectAttributes.addPopupMessage("Решение с ID $solutionId не найдено.")
            return "redirect:$TASK_PATH/$taskId"
        }

        if (solution.task != task) {
            redirectAttributes.addPopupMessage("Решение с ID $solutionId не принадлежит заданию ${task.name}.")
            return "redirect:$TASK_PATH/$taskId"
        }

        if (solution.student != null) {
            redirectAttributes.addPopupMessage("Решение с ID $solutionId не является Эталонным.")
            return "redirect:$TASK_PATH/$taskId"
        }

        val recording = fileManager.getRecordingFilesCompressed(solution)

        val responseEntity = ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"${recording.name}\"")
            .body(recording)

        return responseEntity
    }
    companion object {

        const val TASK_PATH = "$TASKS_PATH/task"
        const val TASK_PAGE = "$DEVELOPER_PAGE/task"

        const val TASK_ATTR = "task"

        const val TASK_FILES_ATTR = "taskFiles"

        const val TEST_RESULTS = "testResults"

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