package trik.testsys.webapp.backoffice.controller.impl.user.developer

import jakarta.servlet.http.HttpSession
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
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.entity.impl.UserGroup
import trik.testsys.webapp.backoffice.data.service.ContestService
import trik.testsys.webapp.backoffice.data.service.StudentGroupService
import trik.testsys.webapp.backoffice.data.service.TaskService
import trik.testsys.webapp.backoffice.data.service.UserGroupService
import trik.testsys.webapp.backoffice.utils.addMessage
import java.time.LocalDateTime
import java.time.ZoneId

@Controller
@RequestMapping("/user/developer")
class DeveloperContestController(
    private val contestService: ContestService,
    private val userGroupService: UserGroupService,
    private val studentGroupService: StudentGroupService,
    private val taskService: TaskService,
) : AbstractUserController() {

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

        setupModel(model, session, developer)
        model.addAttribute("contest", contest)
        model.addAttribute("isOwner", isOwner)
        model.addAttribute("hasAnySolutions", contest.solutions.isNotEmpty())
        model.addAttribute("isAttachedToAnyStudentGroup", studentGroupService.existsByContestId(contest.id!!))
        val taskOrders = contest.getOrders()
        model.addAttribute(
            "availableUserGroups",
            userGroupService.findByMember(developer)
                .filterNot { g -> contest.userGroups.any { it.id == g.id } }
                .sortedBy { it.id }
        )
        model.addAttribute("attachedTasks", contest.tasks.sortedBy { t -> taskOrders[t.id!!] ?: Long.MAX_VALUE })
        model.addAttribute("taskOrders", taskOrders)
        val availableTasks = taskService.findForUser(developer)
            .filterNot { t -> contest.tasks.any { it.id == t.id } }
            .filter { it.testingStatus == Task.TestingStatus.PASSED }
            .sortedBy { it.id }
        model.addAttribute("availableTasks", availableTasks)

        // Build availability info: groups intersection and ownership flag
        val developerGroupIds = developer.memberedGroups.mapNotNull { it.id }.toSet()
        val availableGroupsByTaskId: Map<Long, List<UserGroup>> = availableTasks
            .filter { it.id != null }
            .associate { task ->
                val groups = task.userGroups
                    .filter { ug -> ug.id != null && developerGroupIds.contains(ug.id) }
                    .sortedBy { it.id }
                task.id!! to groups
            }
        val ownedTaskIds: Set<Long> = availableTasks
            .filter { it.developer?.id == developer.id }
            .mapNotNull { it.id }
            .toMutableSet()

        model.addAttribute("availableGroupsByTaskId", availableGroupsByTaskId)
        model.addAttribute("ownedTaskIds", ownedTaskIds)

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
        if (task == null || (task.developer?.id != developer.id && task.userGroups.none { developer.memberedGroups.contains(it) })) {
            redirectAttributes.addMessage("Задача не найдена или недоступна.")
            return "redirect:/user/developer/contests/$id"
        }

        if (task.testingStatus != Task.TestingStatus.PASSED) {
            redirectAttributes.addMessage("Можно прикреплять только Задачи со статусом тестирования Пройдено.")
            return "redirect:/user/developer/contests/$id"
        }

        val hasPolygon = task.taskFiles.any { it.type == TaskFile.TaskFileType.POLYGON }
        val hasSolution = task.taskFiles.any { it.type == TaskFile.TaskFileType.SOLUTION }
        val hasExercise = task.taskFiles.any { it.type == TaskFile.TaskFileType.EXERCISE }
        if (!hasPolygon || !hasSolution || !hasExercise) {
            redirectAttributes.addMessage("Для прикрепления к Туру требуется минимум один Полигон, одно Эталонное Решение и одно Упражнение.")
            return "redirect:/user/developer/contests/$id"
        }

        val added = contest.tasks.add(task)
        if (added && contest.data.orderByTaskId.isNotEmpty()) {
            // Only assign explicit order if ordering was already customized
            val currentOrders = contest.getOrders()
            val nextOrder = (currentOrders.values.maxOrNull() ?: 0L) + 1L
            contest.data.orderByTaskId[task.id!!] = nextOrder
        }
        contestService.save(contest)
        if (added) redirectAttributes.addMessage("Задача прикреплена к Туру.") else redirectAttributes.addMessage("Задача уже была прикреплена.")
        return "redirect:/user/developer/contests/$id"
    }

    @PostMapping("/contests/{id}/groups/add")
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

        val contest = contestService.findById(id) ?: run {
            redirectAttributes.addMessage("Тур не найден.")
            return "redirect:/user/developer/contests"
        }
        if (contest.developer?.id != developer.id) {
            redirectAttributes.addMessage("Доступно только владельцу.")
            return "redirect:/user/developer/contests/$id"
        }

        val group = userGroupService.findById(groupId)
        if (group == null) {
            redirectAttributes.addMessage("Группа не найдена.")
            return "redirect:/user/developer/contests/$id"
        }

        val added = contest.userGroups.add(group)
        contestService.save(contest)
        if (added) redirectAttributes.addMessage("Группа добавлена к доступу.") else redirectAttributes.addMessage("Группа уже имеет доступ.")
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
        if (removed) {
            contest.data.orderByTaskId.remove(taskId)
        }
        contestService.save(contest)
        if (removed) redirectAttributes.addMessage("Задача откреплена от Тура.") else redirectAttributes.addMessage("Задача не была прикреплена.")
        return "redirect:/user/developer/contests/$contestId"
    }

    @PostMapping("/contests/{id}/tasks/order")
    fun updateTaskOrder(
        @PathVariable id: Long,
        @RequestParam("taskId") taskIds: List<Long>,
        @RequestParam("order") orderValues: List<Long>,
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

        if (taskIds.size != orderValues.size) {
            redirectAttributes.addMessage("Некорректные параметры порядка.")
            return "redirect:/user/developer/contests/$id"
        }

        val attachedIds = contest.tasks.mapNotNull { it.id }.toSet()
        val pairs = taskIds.zip(orderValues)
            .filter { (tId, _) -> tId in attachedIds }
            .sortedBy { it.second }

        // normalize to 1..N to avoid duplicates and gaps
        contest.data.orderByTaskId.clear()
        var pos = 1L
        for ((tId, _) in pairs) {
            contest.data.orderByTaskId[tId] = pos
            pos += 1
        }

        contestService.save(contest)
        redirectAttributes.addMessage("Порядок задач обновлён.")
        return "redirect:/user/developer/contests/$id"
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
        @RequestParam(required = false) timezone: String?,
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
            val ldt = LocalDateTime.parse(startsAt)
            val zone = try {
                if (timezone.isNullOrBlank()) ZoneId.systemDefault() else ZoneId.of(timezone)
            } catch (e: Exception) {
                ZoneId.systemDefault()
            }
            ldt.atZone(zone).toInstant()
        } catch (e: Exception) {
            redirectAttributes.addMessage("Некорректная дата начала. Формат: yyyy-MM-dd'T'HH:mm")
            return "redirect:/user/developer/contests/$id"
        }

        val endsInstant = if (!endsAt.isNullOrBlank()) {
            try {
                val ldt = LocalDateTime.parse(endsAt)
                val zone = try {
                    if (timezone.isNullOrBlank()) ZoneId.systemDefault() else ZoneId.of(timezone)
                } catch (e: Exception) {
                    ZoneId.systemDefault()
                }
                ldt.atZone(zone).toInstant()
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

        setupModel(model, session, developer)
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

        val createdContests = contestService.findForOwner(developer).sortedBy { it.id }
        val sharedContests = contestService.findForUser(developer).sortedBy { it.id }

        setupModel(model, session, developer)
        model.addAttribute("createdContests", createdContests)
        model.addAttribute("sharedContests", sharedContests)

        return "developer/contests"
    }

    @PostMapping("/contests/create")
    fun createContest(
        @RequestParam name: String,
        @RequestParam(required = false) info: String?,
        @RequestParam startsAt: String,
        @RequestParam(required = false) endsAt: String?,
        @RequestParam(required = false) duration: Long?,
        @RequestParam(required = false) timezone: String?,
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
            val ldt = LocalDateTime.parse(startsAt)
            val zone = try {
                if (timezone.isNullOrBlank()) ZoneId.systemDefault() else ZoneId.of(timezone)
            } catch (e: Exception) {
                ZoneId.systemDefault()
            }
            ldt.atZone(zone).toInstant()
        } catch (e: Exception) {
            redirectAttributes.addMessage("Некорректная дата начала. Формат: yyyy-MM-dd'T'HH:mm")
            return "redirect:/user/developer/contests"
        }

        val endsInstant = if (!endsAt.isNullOrBlank()) {
            try {
                val ldt = LocalDateTime.parse(endsAt)
                val zone = try {
                    if (timezone.isNullOrBlank()) ZoneId.systemDefault() else ZoneId.of(timezone)
                } catch (e: Exception) {
                    ZoneId.systemDefault()
                }
                ldt.atZone(zone).toInstant()
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

    @PostMapping("/contests/{id}/delete")
    fun deleteContest(
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

        val contest = contestService.findById(id) ?: run {
            redirectAttributes.addMessage("Тур не найден.")
            return "redirect:/user/developer/contests"
        }
        if (contest.developer?.id != developer.id) {
            redirectAttributes.addMessage("Удаление доступно только владельцу.")
            return "redirect:/user/developer/contests/$id"
        }

        val attachedToStudentGroups = studentGroupService.existsByContestId(contest.id!!)
        if (attachedToStudentGroups) {
            redirectAttributes.addMessage("Нельзя удалить Тур, прикреплённый к студенческим группам.")
            return "redirect:/user/developer/contests/$id"
        }

        if (contest.solutions.isNotEmpty()) {
            redirectAttributes.addMessage("Нельзя удалить Тур, по которому есть Решения.")
            return "redirect:/user/developer/contests/$id"
        }

        contestService.delete(contest)
        redirectAttributes.addMessage("Тур удалён.")
        return "redirect:/user/developer/contests"
    }
}
