package trik.testsys.webclient.controller.impl.user.admin

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.impl.main.LoginController
import trik.testsys.webclient.controller.impl.user.admin.AdminGroupController.Companion.GROUP_ATTR
import trik.testsys.webclient.controller.impl.user.admin.AdminMainController.Companion.ADMIN_PATH
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.entity.user.impl.Admin
import trik.testsys.webclient.service.entity.user.impl.AdminService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.view.impl.AdminView
import trik.testsys.webclient.view.impl.GroupCreationView
import trik.testsys.webclient.view.impl.GroupView.Companion.toView
import java.util.*

@Controller
@RequestMapping(AdminGroupsController.GROUPS_PATH)
class AdminGroupsController(
    loginData: LoginData
) : AbstractWebUserController<Admin, AdminView, AdminService>(loginData) {

    override val mainPage = GROUPS_PAGE

    override val mainPath = GROUPS_PATH

    override fun Admin.toView(timeZone: TimeZone) = AdminView(
        id = this.id,
        name = this.name,
        accessToken = this.accessToken,
        creationDate = this.creationDate?.atTimeZone(timeZone),
        lastLoginDate = this.lastLoginDate?.atTimeZone(timeZone),
        viewer = this.viewer,
        additionalInfo = this.additionalInfo,
        groups = this.groups.map { it.toView(timeZone) }.sortedBy { it.id }
    )

    @GetMapping
    fun groupsGet(
        timeZone: TimeZone,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        model.addAttribute(GROUP_ATTR, GroupCreationView.empty())
        model.addAttribute(WEB_USER_ATTR, webUser.toView(timeZone))

        return GROUPS_PAGE
    }

    companion object {

        const val GROUPS_PATH = "$ADMIN_PATH/groups"
        const val GROUPS_PAGE = "admin/groups"
    }
}