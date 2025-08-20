package trik.testsys.webapp.backoffice.controller.impl.user

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PathVariable
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
@RequestMapping("/user")
class UserController(
    private val userGroupService: UserGroupService,
) : AbstractUserController() {

    @GetMapping
    fun getUserPage(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val user = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        // Build dynamic menu from privileges
        val memberedGroups = user.memberedGroups.sortedBy { it.id }
        val privilegesRu = PrivilegeI18n.listRu(user.privileges)

        setupModel(model, session, user)
        model.addAttribute("privilegeToRu", PrivilegeI18n.asMap())
        model.addAttribute("privilegesRu", privilegesRu)
        model.addAttribute("groups", memberedGroups)
        return "user"
    }

    @GetMapping("/groups")
    fun getUserGroupsPage(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        // Route kept for backward compatibility; now the groups are shown on the overview page
        // Redirect to the overview which now contains the groups subsection
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val user = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"
        setupModel(model, session, user)
        return "redirect:/user"
    }

    @GetMapping("/privileges")
    fun getUserPrivilegesPage(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val user = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"
        setupModel(model, session, user)
        return "redirect:/user"
    }

    @PostMapping("/name")
    fun updateName(
        @RequestParam("name") newName: String?,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val user = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        val trimmed = (newName ?: "").trim()
        if (trimmed.isEmpty()) {
            redirectAttributes.addMessage("Псевдоним не может быть пустым.")
            return "redirect:/user"
        }

        userService.updateName(user, trimmed)
        redirectAttributes.addMessage("Псевдоним успешно обновлен.")
        return "redirect:/user"
    }

    @PostMapping("/groups/{id}/leave")
    fun leaveUserGroup(@PathVariable("id") groupId: Long, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val user = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        val group = userGroupService.findById(groupId)
        if (group == null) {
            redirectAttributes.addMessage("Группа не найдена.")
            return "redirect:/user"
        }
        if (!group.members.contains(user)) {
            redirectAttributes.addMessage("Вы не состоите в этой группе.")
            return "redirect:/user"
        }
        if (group.owner?.id == user.id) {
            redirectAttributes.addMessage("Владелец не может покинуть свою группу.")
            return "redirect:/user"
        }
        val ok = userGroupService.removeMember(group, user)
        if (ok) redirectAttributes.addMessage("Вы покинули группу.") else redirectAttributes.addMessage("Не удалось покинуть группу.")
        return "redirect:/user"
    }
}