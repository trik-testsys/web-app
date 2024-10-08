package trik.testsys.webclient.controller.impl.user

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.impl.main.LoginController
import trik.testsys.webclient.controller.user.WebUserController
import trik.testsys.webclient.entity.user.impl.SuperUser
import trik.testsys.webclient.service.entity.user.impl.SuperUserService
import trik.testsys.webclient.service.entity.user.impl.ViewerService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.service.token.access.AccessTokenGenerator
import trik.testsys.webclient.service.token.reg.RegTokenGenerator
import trik.testsys.webclient.util.addPopupMessage
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.view.impl.SuperUserView
import trik.testsys.webclient.view.impl.ViewerCreationView
import java.util.*
import javax.servlet.http.HttpServletRequest

@Controller
@RequestMapping(SuperUserController.SUPER_USER_PATH)
class SuperUserController(
    loginData: LoginData,

    private val viewerService: ViewerService,

    @Qualifier("adminRegTokenGenerator") private val adminRegTokenGenerator: RegTokenGenerator,
    @Qualifier("webUserAccessTokenGenerator") private val webUserAccessTokenGenerator: AccessTokenGenerator
) : WebUserController<SuperUser, SuperUserView, SuperUserService>(loginData) {

    override val MAIN_PATH = SUPER_USER_PATH

    override val MAIN_PAGE = SUPER_USER_PAGE

    override fun SuperUser.toView(timeZone: TimeZone) = SuperUserView(
        id = this.id,
        name = this.name,
        accessToken = this.accessToken,
        creationDate = this.creationDate?.atTimeZone(timeZone),
        lastLoginDate = this.lastLoginDate?.atTimeZone(timeZone),
        additionalInfo = this.additionalInfo,
        viewers = viewerService.findAll().sortedBy { it.id }
    )

    @GetMapping(VIEWERS_PATH)
    fun viewersGet(
        timeZone: TimeZone,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        model.addAttribute(VIEWER_ATTR, ViewerCreationView.empty())
        model.addAttribute(WEB_USER_ATTR, webUser.toView(timeZone))

        return VIEWERS_PAGE
    }

    @PostMapping("$VIEWER_PATH/create")
    fun viewerPost(
        @ModelAttribute("viewer") viewerView: ViewerCreationView,
        timeZone: TimeZone,
        request: HttpServletRequest,
        redirectAttributes: RedirectAttributes
    ): String {
        validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        val regToken = adminRegTokenGenerator.generate(viewerView.name)
        val accessToken = webUserAccessTokenGenerator.generate(viewerView.name)

        val viewer = viewerView.toEntity(accessToken, regToken)
        viewerService.save(viewer)

        redirectAttributes.addPopupMessage("Наблюдатель ${viewer.name} успешно создан.")

        return "redirect:$SUPER_USER_PATH$VIEWERS_PATH"
    }

    companion object {

        const val SUPER_USER_PATH = "/superuser"
        const val SUPER_USER_PAGE = "superuser"

        const val VIEWERS_PATH = "/viewers"
        const val VIEWERS_PAGE = "$SUPER_USER_PAGE/viewers"

        const val VIEWER_PATH = "$VIEWERS_PATH/viewer"
        const val VIEWER_PAGE = "$SUPER_USER_PAGE/viewer"

        const val VIEWER_ATTR = "viewer"
    }
}