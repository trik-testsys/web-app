package trik.testsys.webclient.controller.impl.user

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.user.WebUserController
import trik.testsys.webclient.entity.user.impl.Developer
import trik.testsys.webclient.service.entity.impl.ContestService
import trik.testsys.webclient.service.entity.impl.TaskService
import trik.testsys.webclient.service.entity.user.impl.DeveloperService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.addPopupMessage
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.view.impl.*
import trik.testsys.webclient.view.impl.ContestView.Companion.toView
import trik.testsys.webclient.view.impl.TaskView.Companion.toView
import java.util.*
import javax.servlet.http.HttpServletRequest


@Controller
@RequestMapping(DeveloperController.DEVELOPER_PATH)
class DeveloperController(
    loginData: LoginData,

    private val contestService: ContestService,
    private val taskService: TaskService
) : WebUserController<Developer, DeveloperView, DeveloperService>(loginData) {

    override val MAIN_PATH = DEVELOPER_PATH

    override val MAIN_PAGE = DEVELOPER_PAGE

    override fun Developer.toView(timeZone: TimeZone) = DeveloperView(
        id = this.id,
        name = this.name,
        accessToken = this.accessToken,
        lastLoginDate = this.lastLoginDate?.atTimeZone(timeZone),
        creationDate = this.creationDate?.atTimeZone(timeZone),
        additionalInfo = this.additionalInfo,
        contests = this.contests.map { it.toView(timeZone) }.sortedBy { it.id },
        tasks = this.tasks.map { it.toView(timeZone) }.sortedBy { it.id }
    )

    @GetMapping(CONTESTS_PATH)
    fun contestsGet(
        timeZone: TimeZone,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        model.addAttribute(CONTEST_ATTR, ContestCreationView.empty())
        model.addAttribute(WEB_USER_ATTR, webUser.toView(timeZone))

        return CONTESTS_PAGE
    }

    @PostMapping("$CONTEST_PATH/create")
    fun contestPost(
        @ModelAttribute("contest") contestView: ContestCreationView,
        timeZone: TimeZone,
        request: HttpServletRequest,
        redirectAttributes: RedirectAttributes
    ): String {
        val webUser = validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        val contest = contestView.toEntity(webUser)

        if (!contestService.validateName(contest)) {
            redirectAttributes.addPopupMessage("Название Тура не должно содержать Код-доступа.")
            return "redirect:$DEVELOPER_PATH$CONTESTS_PATH"
        }

        if (!contestService.validateAdditionalInfo(contest)) {
            redirectAttributes.addPopupMessage("Дополнительная информация не должна содержать Код-доступа.")
            return "redirect:$DEVELOPER_PATH$CONTESTS_PATH"
        }

        contestService.save(contest)

        redirectAttributes.addPopupMessage("Тур ${contest.name} успешно создан.")

        return "redirect:$DEVELOPER_PATH$CONTESTS_PATH"
    }

    @GetMapping("$CONTEST_PATH/{contestId}")
    fun contestGet(
        @PathVariable("contestId") contestId: Long,
        timeZone: TimeZone,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        if (!webUser.checkContestExistence(contestId)) {
            redirectAttributes.addPopupMessage("Тур с ID $contestId не найден.")
            return "redirect:$DEVELOPER_PATH$CONTESTS_PATH"
        }

        val contest = contestService.find(contestId) ?: run {
            redirectAttributes.addPopupMessage("Тур с ID $contestId не найден.")
            return "redirect:$DEVELOPER_PATH$CONTESTS_PATH"
        }

        val contestView = contest.toView(timeZone)
        model.addAttribute(CONTEST_ATTR, contestView)

        return CONTEST_PAGE
    }

    @PostMapping("$CONTEST_PATH/update/{contestId}")
    fun contestUpdate(
        @PathVariable("contestId") contestId: Long,
        @ModelAttribute("contest") contestView: ContestView,
        timeZone: TimeZone,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        if (!webUser.checkContestExistence(contestId)) {
            redirectAttributes.addPopupMessage("Тур с ID $contestId не найден.")
            return "redirect:$DEVELOPER_PATH$CONTESTS_PATH"
        }

        val contest = contestView.toEntity(timeZone)
        contest.developer = webUser

        if (!contestService.validateName(contest)) {
            redirectAttributes.addPopupMessage("Название Тура не должно содержать Код-доступа.")
            return "redirect:$DEVELOPER_PATH$CONTEST_PATH/$contestId"
        }

        if (!contestService.validateAdditionalInfo(contest)) {
            redirectAttributes.addPopupMessage("Дополнительная информация не должна содержать Код-доступа.")
            return "redirect:$DEVELOPER_PATH$CONTEST_PATH/$contestId"
        }

        val updatedContest = contestService.save(contest)

        model.addAttribute(CONTEST_ATTR, updatedContest.toView(timeZone))
        redirectAttributes.addPopupMessage("Данные успешно изменены.")

        return "redirect:$DEVELOPER_PATH$CONTEST_PATH/$contestId"
    }

    @GetMapping(TASKS_PATH)
    fun tasksGet(
        timeZone: TimeZone,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        model.addAttribute(TASK_ATTR, ContestCreationView.empty())
        model.addAttribute(WEB_USER_ATTR, webUser.toView(timeZone))

        return TASKS_PAGE
    }

    @PostMapping("$TASK_PATH/create")
    fun taskPost(
        @ModelAttribute("task") taskView: TaskCreationView,
        timeZone: TimeZone,
        request: HttpServletRequest,
        redirectAttributes: RedirectAttributes
    ): String {
        val webUser = validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        val task = taskView.toEntity(webUser)

        if (!taskService.validateName(task)) {
            redirectAttributes.addPopupMessage("Название Задания не должно содержать Код-доступа.")
            return "redirect:$DEVELOPER_PATH$TASKS_PATH"
        }

        if (!taskService.validateAdditionalInfo(task)) {
            redirectAttributes.addPopupMessage("Дополнительная информация не должна содержать Код-доступа.")
            return "redirect:$DEVELOPER_PATH$TASKS_PATH"
        }

        taskService.save(task)

        redirectAttributes.addPopupMessage("Задание ${task.name} успешно создана.")

        return "redirect:$DEVELOPER_PATH$TASKS_PATH"
    }

    @GetMapping("$TASK_PATH/{taskId}")
    fun taskGet(
        @PathVariable("taskId") taskId: Long,
        timeZone: TimeZone,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        if (!webUser.checkTaskExistence(taskId)) {
            redirectAttributes.addPopupMessage("Задание с ID $taskId не найдено.")
            return "redirect:$DEVELOPER_PATH$TASKS_PATH"
        }

        val task = taskService.find(taskId) ?: run {
            redirectAttributes.addPopupMessage("Задание с ID $taskId не найдено.")
            return "redirect:$DEVELOPER_PATH$TASKS_PATH"
        }

        val taskView = task.toView(timeZone)
        model.addAttribute(TASK_ATTR, taskView)

        return TASK_PAGE
    }

    @PostMapping("$TASK_PATH/update/{taskId}")
    fun taskUpdate(
        @PathVariable("taskId") taskId: Long,
        @ModelAttribute("task") taskView: TaskView,
        timeZone: TimeZone,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        if (!webUser.checkTaskExistence(taskId)) {
            redirectAttributes.addPopupMessage("Задание с ID $taskId не найдено.")
            return "redirect:$DEVELOPER_PATH$TASKS_PATH"
        }

        val task = taskView.toEntity(timeZone)
        task.developer = webUser

        if (!taskService.validateName(task)) {
            redirectAttributes.addPopupMessage("Название Задание не должно содержать Код-доступа.")
            return "redirect:$DEVELOPER_PATH$TASK_PATH/$taskId"
        }

        if (!taskService.validateAdditionalInfo(task)) {
            redirectAttributes.addPopupMessage("Дополнительная информация не должна содержать Код-доступа.")
            return "redirect:$DEVELOPER_PATH$TASK_PATH/$taskId"
        }

        val updatedTask = taskService.save(task)

        model.addAttribute(TASK_ATTR, updatedTask.toView(timeZone))
        redirectAttributes.addPopupMessage("Данные успешно изменены.")

        return "redirect:$DEVELOPER_PATH$TASK_PATH/$taskId"
    }

    companion object {

        const val DEVELOPER_PATH = "/developer"
        const val DEVELOPER_PAGE = "developer"

        const val CONTESTS_PATH = "/contests"
        const val CONTESTS_PAGE = "developer/contests"

        const val CONTEST_ATTR = "contest"

        const val CONTEST_PATH = "$CONTESTS_PATH/contest"
        const val CONTEST_PAGE = "developer/contest"

        const val TASKS_PATH = "/tasks"
        const val TASKS_PAGE = "developer/tasks"

        const val TASK_ATTR = "task"

        const val TASK_PATH = "$TASKS_PATH/task"
        const val TASK_PAGE = "developer/task"

        fun Developer.checkContestExistence(contestId: Long?) = contests.any { it.id == contestId }

        fun Developer.checkTaskExistence(taskId: Long?) = tasks.any { it.id == taskId }
    }
}