package trik.testsys.webapp.backoffice.controller.impl.user

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webapp.backoffice.controller.AbstractUserController
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.service.SuperUserService
import trik.testsys.webapp.backoffice.utils.addHasActiveSession
import trik.testsys.webapp.backoffice.utils.addMessage
import trik.testsys.webapp.backoffice.utils.addSections
import trik.testsys.webapp.backoffice.utils.addUser
import trik.testsys.webapp.backoffice.utils.PrivilegeI18n

@Controller
@RequestMapping("/user/superuser")
class SuperUserController(
    private val superUserService: SuperUserService,
) : AbstractUserController() {

    private data class UserRow(
        val id: Long,
        val name: String?,
        val accessToken: String?,
        val privilegesRu: List<String>
    )

    @GetMapping("/users")
    fun usersPage(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val currentUser = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!currentUser.privileges.contains(User.Privilege.SUPER_USER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val sections = menuBuilder.buildFor(currentUser)
        val privilegeOptions = PrivilegeI18n.listOptions()

        val allUsers = userService.findAll().sortedBy { it.id }
        val userRows = allUsers.map { u ->
            val privsRu = PrivilegeI18n.listRu(u.privileges)
            UserRow(id = u.id!!, name = u.name, accessToken = u.accessToken?.value, privilegesRu = privsRu)
        }

        model.apply {
            addHasActiveSession(session)
            addUser(currentUser)
            addSections(sections)
            addAttribute("allUsers", allUsers)
            addAttribute("userRows", userRows)
            addAttribute("privileges", User.Privilege.entries)
            addAttribute("privilegeOptions", privilegeOptions)
            addAttribute("privilegeToRu", User.Privilege.entries.associateWith { PrivilegeI18n.toRu(it) })
        }
        return "superuser/users"
    }

    @GetMapping("/users/create")
    fun createUserForm(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val currentUser = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!currentUser.privileges.contains(User.Privilege.SUPER_USER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val sections = menuBuilder.buildFor(currentUser)
        val privilegeOptions = PrivilegeI18n.listOptions()

        model.apply {
            addHasActiveSession(session)
            addUser(currentUser)
            addSections(sections)
            addAttribute("privilegeOptions", privilegeOptions)
        }
        return "superuser/user-create"
    }

    @PostMapping("/users/create")
    fun createUser(
        @RequestParam name: String?,
        @RequestParam privileges: List<User.Privilege>?,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val currentUser = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!currentUser.privileges.contains(User.Privilege.SUPER_USER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val trimmed = (name ?: "").trim()
        if (trimmed.isEmpty()) {
            redirectAttributes.addMessage("Имя не может быть пустым.")
            return "redirect:/user/superuser/users"
        }

        val privs = privileges?.toSet() ?: emptySet()
        val ok = superUserService.createUser(currentUser, trimmed, privs)
        if (ok) redirectAttributes.addMessage("Пользователь создан.") else redirectAttributes.addMessage("Не удалось создать пользователя.")
        return "redirect:/user/superuser/users"
    }

    @PostMapping("/users/privilege")
    fun addPrivilege(
        @RequestParam userId: Long,
        @RequestParam privilege: User.Privilege,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val currentUser = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!currentUser.privileges.contains(User.Privilege.SUPER_USER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val target = userService.findById(userId)
        if (target == null) {
            redirectAttributes.addMessage("Пользователь не найден.")
            return "redirect:/user/superuser/users"
        }

        val ok = superUserService.addPrivilege(currentUser, target, privilege)
        if (ok) redirectAttributes.addMessage("Роль добавлена.") else redirectAttributes.addMessage("Не удалось добавить роль.")
        return "redirect:/user/superuser/users"
    }
}