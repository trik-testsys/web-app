package trik.testsys.webapp.backoffice.controller.impl.user.admin

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.bind.annotation.RequestParam
import trik.testsys.webapp.backoffice.controller.menu.MenuBuilder
import trik.testsys.webapp.backoffice.data.entity.impl.StudentGroup
import trik.testsys.webapp.backoffice.data.service.impl.AccessTokenService
import trik.testsys.webapp.backoffice.data.service.impl.StudentGroupServiceImpl

@Controller
@RequestMapping("/user/admin/groups")
class AdminGroupsController(
    private val accessTokenService: AccessTokenService,
    private val groupService: StudentGroupServiceImpl,
    private val menuBuilder: MenuBuilder
) {

    @GetMapping
    fun list(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val token = (session.getAttribute("accessToken") as? String)?.let { accessTokenService.findByValue(it) }
            ?: run {
                redirectAttributes.addFlashAttribute("message", "Пожалуйста, войдите в систему.")
                return "redirect:/login"
            }

        val admin = token.user ?: run {
            redirectAttributes.addFlashAttribute("message", "Пользователь не найден.")
            return "redirect:/login"
        }

        val groups = groupService.findAll().filter { it.owner?.id == admin.id }.sortedBy { it.id }

        model.addAttribute("hasActiveSession", true)
        model.addAttribute("user", admin)
        model.addAttribute("menuSections", menuBuilder.buildFor(admin))
        model.addAttribute("groups", groups)
        return "admin/groups"
    }

    @GetMapping("/create")
    fun createForm(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val token = (session.getAttribute("accessToken") as? String)?.let { accessTokenService.findByValue(it) }
            ?: return "redirect:/login"
        val admin = token.user ?: return "redirect:/login"

        model.addAttribute("hasActiveSession", true)
        model.addAttribute("user", admin)
        model.addAttribute("menuSections", menuBuilder.buildFor(admin))
        return "admin/group-create"
    }

    @PostMapping("/create")
    fun create(
        @RequestParam name: String,
        @RequestParam info: String?,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val token = (session.getAttribute("accessToken") as? String)?.let { accessTokenService.findByValue(it) }
            ?: return "redirect:/login"
        val admin = token.user ?: return "redirect:/login"

        val group = StudentGroup().also {
            it.owner = admin
            it.name = name.trim()
            it.info = info?.trim()
            // Owner must not be a member of the group
        }
        groupService.save(group)
        redirectAttributes.addFlashAttribute("message", "Группа создана (id=${group.id}).")
        return "redirect:/user/admin/groups"
    }
}

//package trik.testsys.webapp.backoffice.controller.impl.user.admin
//
//import org.springframework.stereotype.Controller
//import org.springframework.ui.Model
//import org.springframework.web.bind.annotation.CookieValue
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.servlet.mvc.support.RedirectAttributes
//import trik.testsys.backoffice.controller.impl.main.LoginController
//import trik.testsys.backoffice.controller.impl.user.admin.AdminGroupController.Companion.GROUP_ATTR
//import trik.testsys.backoffice.controller.impl.user.admin.AdminMainController.Companion.ADMIN_PATH
//import trik.testsys.backoffice.controller.user.AbstractWebUserController
//import trik.testsys.backoffice.entity.user.impl.Admin
//import trik.testsys.backoffice.service.entity.user.impl.AdminService
//import trik.testsys.backoffice.service.security.login.impl.LoginData
//import trik.testsys.backoffice.util.atTimeZone
//import trik.testsys.backoffice.view.impl.AdminView
//import trik.testsys.backoffice.view.impl.GroupCreationView
//
//@Controller
//@RequestMapping(AdminGroupsController.GROUPS_PATH)
//class AdminGroupsController(
//    loginData: LoginData
//) : AbstractWebUserController<Admin, AdminView, AdminService>(loginData) {
//
//    override val mainPage = GROUPS_PAGE
//
//    override val mainPath = GROUPS_PATH
//
//    override fun Admin.toView(timeZoneId: String?) = AdminView(
//        id = this.id,
//        name = this.name,
//        accessToken = this.accessToken,
//        creationDate = this.creationDate?.atTimeZone(timeZoneId),
//        lastLoginDate = this.lastLoginDate?.atTimeZone(timeZoneId),
//        viewer = this.viewer,
//        additionalInfo = this.additionalInfo,
//        groups = this.groups.map { it.toView(timeZoneId) }.sortedBy { it.id }
//    )
//
//    @GetMapping
//    fun groupsGet(
//        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
//        redirectAttributes: RedirectAttributes,
//        model: Model
//    ): String {
//        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"
//
//        model.addAttribute(GROUP_ATTR, GroupCreationView.empty())
//        model.addAttribute(WEB_USER_ATTR, webUser.toView(timezone))
//
//        return GROUPS_PAGE
//    }
//
//    companion object {
//
//        const val GROUPS_PATH = "$ADMIN_PATH/groups"
//        const val GROUPS_PAGE = "admin/groups"
//    }
//}