package trik.testsys.webclient.controller.impl.user.superuser

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.impl.main.LoginController
import trik.testsys.webclient.controller.impl.user.superuser.SuperUserDevelopersController.Companion.DEVELOPERS_PATH
import trik.testsys.webclient.controller.impl.user.superuser.SuperUserMainController.Companion.SUPER_USER_PAGE
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.entity.user.impl.SuperUser
import trik.testsys.webclient.service.entity.user.impl.DeveloperService
import trik.testsys.webclient.service.entity.user.impl.SuperUserService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.service.token.access.AccessTokenGenerator
import trik.testsys.webclient.util.addPopupMessage
import trik.testsys.webclient.view.impl.DeveloperCreationView
import trik.testsys.webclient.view.impl.SuperUserView
import java.util.*
import javax.servlet.http.HttpServletRequest

/**
 * @author Roman Shishkin
 * @since 2.1.0
 */
@Controller
@RequestMapping(SuperUserDeveloperController.DEVELOPER_PATH)
class SuperUserDeveloperController(
    loginData: LoginData,

    private val developerService: DeveloperService,
    @Qualifier("webUserAccessTokenGenerator") private val webUserAccessTokenGenerator: AccessTokenGenerator,
) : AbstractWebUserController<SuperUser, SuperUserView, SuperUserService>(loginData) {

    override val mainPage = DEVELOPER_PAGE

    override val mainPath = DEVELOPER_PATH

    override fun SuperUser.toView(timeZoneId: String?) = TODO()

    @PostMapping("/create")
    fun developerPost(
        @ModelAttribute("developer") developerView: DeveloperCreationView,
        timeZone: TimeZone,
        request: HttpServletRequest,
        redirectAttributes: RedirectAttributes
    ): String {
        loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        val accessToken = webUserAccessTokenGenerator.generate(developerView.name)

        val developer = developerView.toEntity(accessToken)
        developerService.save(developer)

        redirectAttributes.addPopupMessage("Разработчик ${developer.name} успешно создан.")

        return "redirect:$DEVELOPERS_PATH"
    }

    companion object {

        const val DEVELOPER_PATH = "$DEVELOPERS_PATH/developer"
        const val DEVELOPER_PAGE = "$SUPER_USER_PAGE/developer"

        const val DEVELOPER_ATTR = "developer"
    }
}