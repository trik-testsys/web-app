package trik.testsys.webclient.controller.impl.user.superuser

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.impl.main.LoginController
import trik.testsys.webclient.controller.impl.user.superuser.SuperUserMainController.Companion.SUPER_USER_PAGE
import trik.testsys.webclient.controller.impl.user.superuser.SuperUserAdminsController.Companion.ADMINS_PATH
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.entity.user.impl.SuperUser
import trik.testsys.webclient.service.entity.user.impl.SuperUserService
import trik.testsys.webclient.service.entity.user.impl.AdminService
import trik.testsys.webclient.service.entity.user.impl.ViewerService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.service.token.access.AccessTokenGenerator
import trik.testsys.webclient.util.addPopupMessage
import trik.testsys.webclient.view.impl.SuperUserView
import trik.testsys.webclient.view.impl.AdminCreationView
import java.util.*
import javax.servlet.http.HttpServletRequest

@Controller
@RequestMapping(SuperUserAdminController.ADMIN_PATH)
class SuperUserAdminController(
    loginData: LoginData,

    private val adminService: AdminService,
    private val viewerService: ViewerService,

    @Qualifier("webUserAccessTokenGenerator") private val webUserAccessTokenGenerator: AccessTokenGenerator,
) : AbstractWebUserController<SuperUser, SuperUserView, SuperUserService>(loginData) {

    override val mainPage = ADMIN_PAGE

    override val mainPath = ADMIN_PATH

    override fun SuperUser.toView(timeZoneId: String?) = TODO()

    @PostMapping("/create")
    fun adminPost(
        @ModelAttribute("admin") adminView: AdminCreationView,
        timeZone: TimeZone,
        request: HttpServletRequest,
        redirectAttributes: RedirectAttributes
    ): String {
        loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        val accessToken = webUserAccessTokenGenerator.generate(adminView.name)

        val admin = adminView.toEntity(accessToken, viewerService)
        adminService.save(admin)

        redirectAttributes.addPopupMessage("Организатор ${admin.name} успешно создан.")

        return "redirect:$ADMINS_PATH"
    }

    companion object {

        const val ADMIN_PATH = "$ADMINS_PATH/admin"
        const val ADMIN_PAGE = "$SUPER_USER_PAGE/admin"

        const val ADMIN_ATTR = "admin"
    }
}