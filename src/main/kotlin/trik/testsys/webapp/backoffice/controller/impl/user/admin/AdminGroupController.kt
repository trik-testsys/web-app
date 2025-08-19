package trik.testsys.webapp.backoffice.controller.impl.user.admin

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webapp.backoffice.controller.menu.MenuBuilder
import trik.testsys.webapp.backoffice.data.service.impl.AccessTokenService
import trik.testsys.webapp.backoffice.data.service.impl.StudentGroupServiceImpl
import trik.testsys.webapp.backoffice.data.service.impl.UserServiceImpl
import trik.testsys.webapp.backoffice.data.entity.impl.User
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

@Controller
@RequestMapping("/user/admin/groups")
class AdminGroupController(
    private val accessTokenService: AccessTokenService,
    private val groupService: StudentGroupServiceImpl,
    private val userService: UserServiceImpl,
    private val menuBuilder: MenuBuilder
) {

    @GetMapping("/{id}")
    fun view(
        @PathVariable id: Long,
        model: Model,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val token = (session.getAttribute("accessToken") as? String)?.let { accessTokenService.findByValue(it) }
            ?: return "redirect:/login"
        val admin = token.user ?: return "redirect:/login"

        val group = groupService.findById(id) ?: run {
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

    @PostMapping("/{id}/generate")
    fun generateStudents(
        @PathVariable id: Long,
        @RequestParam(name = "count", defaultValue = "1") count: Int,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val token = (session.getAttribute("accessToken") as? String)?.let { accessTokenService.findByValue(it) }
            ?: return "redirect:/login"
        val admin = token.user ?: return "redirect:/login"

        val group = groupService.findById(id) ?: return "redirect:/user/admin/groups"
        if (group.owner?.id != admin.id) return "redirect:/user/admin/groups"

        if (count < 1 || count > 200) {
            redirectAttributes.addFlashAttribute("message", "Количество должно быть от 1 до 200.")
            return "redirect:/user/admin/groups/$id"
        }

        // Generate users with STUDENT privilege and add them to the group
        repeat(count) { idx ->
            val token = accessTokenService.generate(admin.id)
            val student = User().also {
                it.name = "st-${group.id}-${System.currentTimeMillis()}-${idx + 1}"
                it.accessToken = token
                it.privileges.add(User.Privilege.STUDENT)
            }
            // Persist user first
            val persisted = userService.save(student)
            // Link inverse side and persist token
            token.user = persisted
            accessTokenService.save(token)
            // Add to group members
            group.members.add(persisted)
        }
        groupService.save(group)
        redirectAttributes.addFlashAttribute("message", "Сгенерировано ${count} участников.")
        return "redirect:/user/admin/groups/$id"
    }

    @GetMapping("/{id}/export")
    fun exportStudents(
        @PathVariable id: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): ResponseEntity<ByteArray> {
        val token = (session.getAttribute("accessToken") as? String)?.let { accessTokenService.findByValue(it) }
            ?: return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/login").build()
        val admin = token.user ?: return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/login").build()

        val group = groupService.findById(id) ?: return ResponseEntity.badRequest().build()
        if (group.owner?.id != admin.id) return ResponseEntity.status(403).build()

        val header = "user_id,user_name,access_token\n"
        val body = group.members.joinToString("\n") { (it.id ?: 0).toString() + "," + (it.name ?: "") + "," + (it.accessToken?.value ?: "") }
        val csv = (header + body).toByteArray()
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=group_${group.id}_students.csv")
            .contentType(MediaType.TEXT_PLAIN)
            .body(csv)
    }
}
