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

@Controller
@RequestMapping("/user")
class UserController(
    private val userGroupService: UserGroupService,
) : AbstractUserController() {

    private data class PrivRow(
        val role: String,
        val localized: String,
        val authorities: String,
        val link: String?
    )

    @GetMapping
    fun getUserPage(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val user = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        // Build dynamic menu from privileges
        val sections = menuBuilder.buildFor(user)
        val memberedGroups = user.memberedGroups.sortedBy { it.id }

        val privilegeToRu = mapOf(
            User.Privilege.ADMIN to "Организатор",
            User.Privilege.DEVELOPER to "Разработчик",
            User.Privilege.JUDGE to "Судья",
            User.Privilege.STUDENT to "Участник",
            User.Privilege.SUPER_USER to "Супервайзер",
            User.Privilege.VIEWER to "Наблюдатель",
            User.Privilege.GROUP_ADMIN to "Администратор Групп",
        )

        val privilegesRu = user.privileges.map { privilegeToRu[it] ?: it.name }.sorted()

        model.apply {
            addHasActiveSession(session)
            addUser(user)
            addSections(sections)
            addAttribute("privilegeToRu", privilegeToRu)
            addAttribute("privilegesRu", privilegesRu)
            addAttribute("groups", memberedGroups)
        }
        return "user"
    }

    @GetMapping("/groups")
    fun getUserGroupsPage(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        // Route kept for backward compatibility; now the groups are shown on the overview page
        // Redirect to the overview which now contains the groups subsection
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val user = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"
        val sections = menuBuilder.buildFor(user)
        model.apply {
            addHasActiveSession(session)
            addUser(user)
            addSections(sections)
        }
        return "redirect:/user"
    }

    @GetMapping("/privileges")
    fun getUserPrivilegesPage(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val user = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        val sections = menuBuilder.buildFor(user)
        val privileges = user.privileges.map { privilege ->
            when (privilege) {
                User.Privilege.ADMIN -> PrivRow(
                    role = "ADMIN",
                    localized = "Организатор",
                    authorities = "Организация соревнований, создание Групп",
                    link = "/user/admin/groups"
                )
                User.Privilege.DEVELOPER -> PrivRow(
                    role = "DEVELOPER",
                    localized = "Разработчик Задач",
                    authorities = "Управление Задачами и Турами",
                    link = null
                )
                User.Privilege.JUDGE -> PrivRow(
                    role = "JUDGE",
                    localized = "Судья",
                    authorities = "Принятие аппеляций и изменение вердиктов решений",
                    link = null
                )
                User.Privilege.STUDENT -> PrivRow(
                    role = "STUDENT",
                    localized = "Участник",
                    authorities = "Участие в Турах",
                    link = null
                )
                User.Privilege.SUPER_USER -> PrivRow(
                    role = "SUPER_USER",
                    localized = "Супервайзер",
                    authorities = "Управление Пользователями и их Ролями",
                    link = "/user/superuser/users"
                )
                User.Privilege.VIEWER -> PrivRow(
                    role = "VIEWER",
                    localized = "Наблюдатель",
                    authorities = "Просмотр результатов, приглашение Организаторов",
                    link = "/user/viewer/admins"
                )
                User.Privilege.GROUP_ADMIN -> PrivRow(
                    role = "GROUP_ADMIN",
                    localized = "Администратор Групп",
                    authorities = "Создание и управление Группами Пользователей",
                    link = "/user/group-admin/groups"
                )
            }
        }.sortedBy { it.role }

        model.apply {
            addHasActiveSession(session)
            addUser(user)
            addSections(sections)
            addAttribute("privRows", privileges)
        }
        return "user/privileges"
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
        val ok = userGroupService.removeMember(group, user)
        if (ok) redirectAttributes.addMessage("Вы покинули группу.") else redirectAttributes.addMessage("Не удалось покинуть группу.")
        return "redirect:/user"
    }
}