package trik.testsys.webapp.backoffice.controller.impl.user

import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
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
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.service.StudentGroupService
import trik.testsys.webapp.backoffice.data.service.ContestService
import trik.testsys.webapp.backoffice.utils.addMessage
import trik.testsys.webapp.backoffice.utils.PrivilegeI18n

@Controller
@RequestMapping("/user/admin")
class AdminController(
    private val studentGroupService: StudentGroupService,
    private val contestService: ContestService,
) : AbstractUserController() {

    @GetMapping("/groups")
    fun groupsList(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val admin = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        val groups = studentGroupService.findByOwner(admin).sortedBy { it.id }

        setupModel(model, session, admin)
        model.addAttribute("groups", groups)
        model.addAttribute("privilegeToRu", PrivilegeI18n.asMap())

        return "admin/groups"
    }

    @GetMapping("/groups/{id}")
    fun viewGroup(
        @PathVariable id: Long,
        model: Model,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val admin = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        val group = studentGroupService.findById(id) ?: run {
            redirectAttributes.addMessage("Группа не найдена.")
            return "redirect:/user/admin/groups"
        }
        if (group.owner?.id != admin.id) {
            redirectAttributes.addMessage("Нет доступа к группе.")
            return "redirect:/user/admin/groups"
        }

        val memberPrivilegesRuByUserId = group.members.associate { it.id!! to PrivilegeI18n.listRu(it.privileges) }

        setupModel(model, session, admin)
        model.addAttribute("group", group)
        model.addAttribute("memberPrivilegesRuByUserId", memberPrivilegesRuByUserId)
        model.addAttribute("privilegeToRu", PrivilegeI18n.asMap())
        model.addAttribute("attachedContests", group.contests.sortedBy { it.id })
        model.addAttribute(
            "availableContests",
            contestService.findForUser(admin)
                .filterNot { c -> group.contests.any { it.id == c.id } }
                .sortedBy { it.id }
        )

        return "admin/group"
    }

    @GetMapping("/groups/create")
    fun createForm(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val admin = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        setupModel(model, session, admin)

        return "admin/group-create"
    }

    @PostMapping("/groups/create")
    fun create(
        @RequestParam name: String,
        @RequestParam info: String?,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val admin = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        val group = studentGroupService.create(admin, name, info) ?: run {
            redirectAttributes.addMessage("Ошибка при создании Группы.")
            return "redirect:/user/admin/groups/create"
        }

        redirectAttributes.addMessage("Группа создана (id=${group.id}).")
        return "redirect:/user/admin/groups"
    }

    @PostMapping("/groups/{id}/generate")
    fun generateStudents(
        @PathVariable id: Long,
        @RequestParam(name = "count", defaultValue = "1") count: Int,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val admin = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        val group = studentGroupService.findById(id) ?: return "redirect:/user/admin/groups"
        if (group.owner?.id != admin.id) return "redirect:/user/admin/groups"

        if (count < 1 || count > 200) {
            redirectAttributes.addMessage("Количество должно быть от 1 до 200.")
            return "redirect:/user/admin/groups/$id"
        }

        val created = studentGroupService.generateStudents(admin, group, count)
        redirectAttributes.addMessage("Сгенерировано ${created.size} участников.")
        return "redirect:/user/admin/groups/$id"
    }

    @GetMapping("/groups/{id}/export")
    fun exportStudents(
        @PathVariable id: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): ResponseEntity<ByteArray> {
        val redirection: ResponseEntity<ByteArray> = ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .header(HttpHeaders.LOCATION, "/login")
            .build()

        val accessToken = getAccessToken(session, redirectAttributes) ?: return redirection
        val admin = getUser(accessToken, redirectAttributes) ?: return redirection

        val group = studentGroupService.findById(id) ?: return ResponseEntity.badRequest().build()
        if (group.owner?.id != admin.id) return ResponseEntity.status(403).build()

        val csv = studentGroupService.generateMembersCsv(group)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=group_${group.id}_students.csv")
            .contentType(MediaType.TEXT_PLAIN)
            .body(csv)
    }

    @GetMapping("/groups/{id}/export-results")
    fun exportResults(
        @PathVariable id: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): ResponseEntity<ByteArray> {
        val redirection: ResponseEntity<ByteArray> = ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .header(HttpHeaders.LOCATION, "/login")
            .build()

        val accessToken = getAccessToken(session, redirectAttributes) ?: return redirection
        val admin = getUser(accessToken, redirectAttributes) ?: return redirection

        val group = studentGroupService.findById(id) ?: return ResponseEntity.badRequest().build()
        if (group.owner?.id != admin.id) return ResponseEntity.status(403).build()

        val csv = studentGroupService.generateResultsCsv(group)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=group_${group.id}_results.csv")
            .contentType(MediaType.TEXT_PLAIN)
            .body(csv)
    }

    @PostMapping("/groups/{id}/attach-contest")
    fun attachContest(
        @PathVariable id: Long,
        @RequestParam("contestId") contestId: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val admin = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        val group = studentGroupService.findById(id) ?: return "redirect:/user/admin/groups"
        if (group.owner?.id != admin.id) return "redirect:/user/admin/groups"

        val contest = contestService.findById(contestId)
        if (contest == null) {
            redirectAttributes.addMessage("Тур не найден.")
            return "redirect:/user/admin/groups/$id"
        }

        val available = contestService.findForUser(admin)
        val canAttach = available.any { it.id == contest.id }
        if (!canAttach) {
            redirectAttributes.addMessage("Вы не имеете доступа к этому Туру.")
            return "redirect:/user/admin/groups/$id"
        }

        val added = group.contests.add(contest)
        studentGroupService.save(group)
        if (added) {
            redirectAttributes.addMessage("Тур прикреплён к группе.")
        } else {
            redirectAttributes.addMessage("Тур уже прикреплён к группе.")
        }
        return "redirect:/user/admin/groups/$id"
    }

    @PostMapping("/groups/{id}/detach-contest")
    fun detachContest(
        @PathVariable id: Long,
        @RequestParam("contestId") contestId: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val admin = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        val group = studentGroupService.findById(id) ?: return "redirect:/user/admin/groups"
        if (group.owner?.id != admin.id) return "redirect:/user/admin/groups"

        val contest = contestService.findById(contestId)
        if (contest == null) {
            redirectAttributes.addMessage("Тур не найден.")
            return "redirect:/user/admin/groups/$id"
        }

        val removed = group.contests.removeIf { it.id == contest.id }
        studentGroupService.save(group)
        if (removed) {
            redirectAttributes.addMessage("Тур откреплён от группы.")
        } else {
            redirectAttributes.addMessage("Тур не был прикреплён к группе.")
        }
        return "redirect:/user/admin/groups/$id"
    }
}