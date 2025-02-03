package trik.testsys.webclient.controller.impl.user.superuser

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.impl.main.LoginController
import trik.testsys.webclient.controller.impl.user.superuser.SuperUserMainController.Companion.SUPER_USER_PAGE
import trik.testsys.webclient.controller.impl.user.superuser.SuperUserMainController.Companion.SUPER_USER_PATH
import trik.testsys.webclient.controller.impl.user.superuser.SuperUserAdminController.Companion.ADMIN_ATTR
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.entity.user.impl.SuperUser
import trik.testsys.webclient.service.entity.user.impl.SuperUserService
import trik.testsys.webclient.service.entity.user.impl.AdminService
import trik.testsys.webclient.service.entity.user.impl.ViewerService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.view.impl.SuperUserView
import trik.testsys.webclient.view.impl.AdminCreationView

@Controller
@RequestMapping(SuperUserAdminsController.ADMINS_PATH)
class SuperUserAdminsController(
    loginData: LoginData,

    private val adminService: AdminService,
    private val viewersService: ViewerService
) : AbstractWebUserController<SuperUser, SuperUserView, SuperUserService>(loginData) {

    override val mainPage = ADMINS_PAGE

    override val mainPath = ADMINS_PATH

    override fun SuperUser.toView(timeZoneId: String?) = SuperUserView(
        id = this.id,
        name = this.name,
        accessToken = this.accessToken,
        creationDate = this.creationDate?.atTimeZone(timeZoneId),
        lastLoginDate = this.lastLoginDate?.atTimeZone(timeZoneId),
        additionalInfo = this.additionalInfo,
        admins = adminService.findAll().sortedBy { it.id }
    )

    @GetMapping
    fun adminsGet(
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        model.addAttribute(ADMIN_ATTR, AdminCreationView.empty())
        model.addAttribute(WEB_USER_ATTR, webUser.toView(timezone))

        val viewers = viewersService.findAll().sortedBy { it.id }
        model.addAttribute(VIEWERS_ATTR, viewers)

        return ADMINS_PAGE
    }

    companion object {

        const val ADMINS_PATH = "$SUPER_USER_PATH/admins"
        const val ADMINS_PAGE = "$SUPER_USER_PAGE/admins"

        const val VIEWERS_ATTR = "viewers"
    }
}