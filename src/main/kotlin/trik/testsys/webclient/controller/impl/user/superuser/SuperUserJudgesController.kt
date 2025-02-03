package trik.testsys.webclient.controller.impl.user.superuser

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.impl.main.LoginController
import trik.testsys.webclient.controller.impl.user.superuser.SuperUserJudgeController.Companion.JUDGE_ATTR
import trik.testsys.webclient.controller.impl.user.superuser.SuperUserMainController.Companion.SUPER_USER_PAGE
import trik.testsys.webclient.controller.impl.user.superuser.SuperUserMainController.Companion.SUPER_USER_PATH
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.entity.user.impl.SuperUser
import trik.testsys.webclient.service.entity.user.impl.JudgeService
import trik.testsys.webclient.service.entity.user.impl.SuperUserService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.view.impl.JudgeCreationView
import trik.testsys.webclient.view.impl.SuperUserView

/**
 * @author Roman Shishkin
 * @since 2.1.0
 */
@Controller
@RequestMapping(SuperUserJudgesController.JUDGES_PATH)
class SuperUserJudgesController(
    loginData: LoginData,

    private val judgeService: JudgeService
) : AbstractWebUserController<SuperUser, SuperUserView, SuperUserService>(loginData) {

    override val mainPage = JUDGES_PAGE

    override val mainPath = JUDGES_PATH

    override fun SuperUser.toView(timeZoneId: String?) = SuperUserView(
        id = this.id,
        name = this.name,
        accessToken = this.accessToken,
        creationDate = this.creationDate?.atTimeZone(timeZoneId),
        lastLoginDate = this.lastLoginDate?.atTimeZone(timeZoneId),
        additionalInfo = this.additionalInfo,
        judges = judgeService.findAll().sortedBy { it.id }
    )

    @GetMapping
    fun judgesGet(
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        model.addAttribute(JUDGE_ATTR, JudgeCreationView.empty())
        model.addAttribute(WEB_USER_ATTR, webUser.toView(timezone))

        return JUDGES_PATH
    }

    companion object {

        const val JUDGES_PATH = "$SUPER_USER_PATH/judges"
        const val JUDGES_PAGE = "$SUPER_USER_PAGE/judges"
    }
}