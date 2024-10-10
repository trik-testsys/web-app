package trik.testsys.webclient.controller.impl.user.superuser

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.impl.main.LoginController
import trik.testsys.webclient.controller.impl.user.superuser.SuperUserMainController.Companion.SUPER_USER_PAGE
import trik.testsys.webclient.controller.impl.user.superuser.SuperUserViewersController.Companion.VIEWERS_PATH
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.entity.user.impl.SuperUser
import trik.testsys.webclient.service.entity.user.impl.SuperUserService
import trik.testsys.webclient.service.entity.user.impl.ViewerService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.service.token.access.AccessTokenGenerator
import trik.testsys.webclient.service.token.reg.RegTokenGenerator
import trik.testsys.webclient.util.addPopupMessage
import trik.testsys.webclient.view.impl.SuperUserView
import trik.testsys.webclient.view.impl.ViewerCreationView
import java.util.*
import javax.servlet.http.HttpServletRequest

@Controller
@RequestMapping(SuperUserViewerController.VIEWER_PATH)
class SuperUserViewerController(
    loginData: LoginData,

    private val viewerService: ViewerService,

    @Qualifier("adminRegTokenGenerator") private val adminRegTokenGenerator: RegTokenGenerator,
    @Qualifier("webUserAccessTokenGenerator") private val webUserAccessTokenGenerator: AccessTokenGenerator,
) : AbstractWebUserController<SuperUser, SuperUserView, SuperUserService>(loginData) {

    override val mainPage = VIEWER_PAGE

    override val mainPath = VIEWER_PATH

    override fun SuperUser.toView(timeZoneId: String?) = TODO()

    @PostMapping("/create")
    fun viewerPost(
        @ModelAttribute("viewer") viewerView: ViewerCreationView,
        timeZone: TimeZone,
        request: HttpServletRequest,
        redirectAttributes: RedirectAttributes
    ): String {
        loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        val regToken = adminRegTokenGenerator.generate(viewerView.name)
        val accessToken = webUserAccessTokenGenerator.generate(viewerView.name)

        val viewer = viewerView.toEntity(accessToken, regToken)
        viewerService.save(viewer)

        redirectAttributes.addPopupMessage("Наблюдатель ${viewer.name} успешно создан.")

        return "redirect:$VIEWERS_PATH"
    }

    companion object {

        const val VIEWER_PATH = "$VIEWERS_PATH/viewer"
        const val VIEWER_PAGE = "$SUPER_USER_PAGE/viewer"

        const val VIEWER_ATTR = "viewer"

        private val logger = LoggerFactory.getLogger(SuperUserViewerController::class.java)
    }
}