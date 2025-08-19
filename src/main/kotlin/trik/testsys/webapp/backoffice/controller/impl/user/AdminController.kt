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
import trik.testsys.webapp.backoffice.data.service.StudentGroupService
import trik.testsys.webapp.backoffice.util.getRedirection

@Controller
@RequestMapping("/user/admin")
class AdminController(
    private val studentGroupService: StudentGroupService,
) : AbstractUserController() {

    @GetMapping("/groups")
    fun groupsList(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val admin = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        val groups = studentGroupService.findByOwner(admin).sortedBy { it.id }

        model.addAttribute("hasActiveSession", true)
        model.addAttribute("user", admin)
        model.addAttribute("menuSections", menuBuilder.buildFor(admin))
        model.addAttribute("groups", groups)
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
            redirectAttributes.addFlashAttribute("message", "Группа не найдена.")
            return "redirect:/user/admin/groups"
        }
        if (group.owner?.id != admin.id) {
            redirectAttributes.addFlashAttribute("message", "Нет доступа к группе.")
            return "redirect:/user/admin/groups"
        }

        model.addAttribute("hasActiveSession", true)
        model.addAttribute("user", admin)
        model.addAttribute("menuSections", menuBuilder.buildFor(admin))
        model.addAttribute("group", group)
        return "admin/group"
    }

    @GetMapping("/groups/create")
    fun createForm(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val admin = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        model.addAttribute("hasActiveSession", true)
        model.addAttribute("user", admin)
        model.addAttribute("menuSections", menuBuilder.buildFor(admin))
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
            redirectAttributes.addFlashAttribute("message", "Ошибка при создании Группы.")
            return "redirect:/user/admin/groups/create"
        }

        redirectAttributes.addFlashAttribute("message", "Группа создана (id=${group.id}).")
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
            redirectAttributes.addFlashAttribute("message", "Количество должно быть от 1 до 200.")
            return "redirect:/user/admin/groups/$id"
        }

        val created = studentGroupService.generateStudents(admin, group, count)
        redirectAttributes.addFlashAttribute("message", "Сгенерировано ${created.size} участников.")
        return "redirect:/user/admin/groups/$id"
    }

    @GetMapping("/groups/{id}/export")
    fun exportStudents(
        @PathVariable id: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): ResponseEntity<ByteArray> {
        val redirection = getRedirection<ByteArray>(HttpStatus.UNAUTHORIZED, "/login")

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
}