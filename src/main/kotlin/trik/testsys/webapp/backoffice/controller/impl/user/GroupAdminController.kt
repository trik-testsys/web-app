package trik.testsys.webapp.backoffice.controller.impl.user

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
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.service.UserGroupService
import trik.testsys.webapp.backoffice.utils.addHasActiveSession
import trik.testsys.webapp.backoffice.utils.addMessage
import trik.testsys.webapp.backoffice.utils.addSections
import trik.testsys.webapp.backoffice.utils.addUser
import trik.testsys.webapp.backoffice.utils.PrivilegeI18n

@Controller
@RequestMapping("/user/group-admin")
class GroupAdminController(
    private val userGroupService: UserGroupService,
) : AbstractUserController() {

    @GetMapping("/groups")
    fun groupsList(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val current = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!current.privileges.contains(User.Privilege.GROUP_ADMIN)) {
            redirectAttributes.addMessage("Нет прав GROUP_ADMIN.")
            return "redirect:/user"
        }

        val groups = userGroupService.findByOwner(current).sortedBy { it.id }

        setupModel(model, session, current)
        model.addAttribute("groups", groups)
        model.addAttribute("privilegeToRu", PrivilegeI18n.asMap())

        return "group-admin/groups"
    }

    @GetMapping("/groups/{id}")
    fun viewGroup(@PathVariable id: Long, model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val current = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        val group = userGroupService.findById(id) ?: run {
            redirectAttributes.addMessage("Группа не найдена.")
            return "redirect:/user/group-admin/groups"
        }
        if (group.owner?.id != current.id) {
            redirectAttributes.addMessage("Нет доступа к группе.")
            return "redirect:/user/group-admin/groups"
        }

        val memberPrivilegesRuByUserId = group.members.associate { it.id!! to PrivilegeI18n.listRu(it.privileges) }

        // Build a list of candidate users to add: exclude owner and already added members
        val candidateUsers = userService.findAll()
            .filter { user -> user.id != group.owner?.id && !group.members.contains(user) }
            .sortedBy { it.name?.lowercase() ?: "" }

        setupModel(model, session, current)
        model.addAttribute("group", group)
        model.addAttribute("memberPrivilegesRuByUserId", memberPrivilegesRuByUserId)
        model.addAttribute("privilegeToRu", PrivilegeI18n.asMap())
        model.addAttribute("candidateUsers", candidateUsers)

        return "group-admin/group"
    }

    @GetMapping("/groups/create")
    fun createForm(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val current = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        setupModel(model, session, current)

        return "group-admin/group-create"
    }

    @PostMapping("/groups/create")
    fun create(@RequestParam name: String, @RequestParam info: String?, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val current = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        val group = userGroupService.create(current, name, info) ?: run {
            redirectAttributes.addMessage("Ошибка при создании Группы.")
            return "redirect:/user/group-admin/groups/create"
        }

        redirectAttributes.addMessage("Группа создана (id=${group.id}).")
        return "redirect:/user/group-admin/groups"
    }

    @PostMapping("/groups/{id}/add-member")
    fun addMember(@PathVariable id: Long, @RequestParam userId: Long, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val current = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        val group = userGroupService.findById(id) ?: return "redirect:/user/group-admin/groups"
        if (group.owner?.id != current.id) return "redirect:/user/group-admin/groups"

        val user = userService.findById(userId)
        if (user == null) {
            redirectAttributes.addMessage("Пользователь не найден.")
        } else {
            if (group.members.contains(user)) {
                redirectAttributes.addMessage("Пользователь уже в группе.")
            } else {
                val ok = userGroupService.addMember(group, user)
                if (ok) redirectAttributes.addMessage("Пользователь добавлен.") else redirectAttributes.addMessage("Не удалось добавить пользователя.")
            }
        }
        return "redirect:/user/group-admin/groups/$id"
    }

    @PostMapping("/groups/{id}/remove-member")
    fun removeMember(@PathVariable id: Long, @RequestParam userId: Long, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val current = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        val group = userGroupService.findById(id) ?: return "redirect:/user/group-admin/groups"
        if (group.owner?.id != current.id) return "redirect:/user/group-admin/groups"

        // Prevent removing the owner from the group
        if (group.owner?.id == userId) {
            redirectAttributes.addMessage("Нельзя удалить владельца группы.")
            return "redirect:/user/group-admin/groups/$id"
        }

        val user = userService.findById(userId)
        if (user == null) {
            redirectAttributes.addMessage("Пользователь не найден.")
        } else {
            val ok = userGroupService.removeMember(group, user)
            if (ok) redirectAttributes.addMessage("Пользователь удален.") else redirectAttributes.addMessage("Не удалось удалить пользователя.")
        }
        return "redirect:/user/group-admin/groups/$id"
    }
}


