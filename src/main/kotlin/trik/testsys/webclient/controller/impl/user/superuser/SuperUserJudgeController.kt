package trik.testsys.webclient.controller.impl.user.superuser

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.impl.main.LoginController
import trik.testsys.webclient.controller.impl.user.superuser.SuperUserJudgesController.Companion.JUDGES_PATH
import trik.testsys.webclient.controller.impl.user.superuser.SuperUserMainController.Companion.SUPER_USER_PAGE
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.entity.user.impl.SuperUser
import trik.testsys.webclient.service.entity.user.impl.JudgeService
import trik.testsys.webclient.service.entity.user.impl.SuperUserService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.service.token.access.AccessTokenGenerator
import trik.testsys.webclient.util.addPopupMessage
import trik.testsys.webclient.view.impl.JudgeCreationView
import trik.testsys.webclient.view.impl.SuperUserView
import java.util.*
import javax.servlet.http.HttpServletRequest

/**
 * @author Roman Shishkin
 * @since 2.1.0
 */
@Controller
@RequestMapping(SuperUserJudgeController.JUDGE_PATH)
class SuperUserJudgeController(
    loginData: LoginData,

    private val judgeService: JudgeService,
    @Qualifier("webUserAccessTokenGenerator") private val webUserAccessTokenGenerator: AccessTokenGenerator,
) : AbstractWebUserController<SuperUser, SuperUserView, SuperUserService>(loginData) {

    override val mainPage = JUDGE_PAGE

    override val mainPath = JUDGE_PATH

    override fun SuperUser.toView(timeZoneId: String?) = TODO()

    @PostMapping("/create")
    fun judgePost(
        @ModelAttribute("judge") judgeView: JudgeCreationView,
        timeZone: TimeZone,
        request: HttpServletRequest,
        redirectAttributes: RedirectAttributes
    ): String {
        loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        val accessToken = webUserAccessTokenGenerator.generate(judgeView.name)

        val judge = judgeView.toEntity(accessToken)
        judgeService.save(judge)

        redirectAttributes.addPopupMessage("Судья ${judge.name} успешно создан.")

        return "redirect:$JUDGES_PATH"
    }

    companion object {

        const val JUDGE_PATH = "$JUDGES_PATH/judge"
        const val JUDGE_PAGE = "$SUPER_USER_PAGE/judge"

        const val JUDGE_ATTR = "judge"
    }
}