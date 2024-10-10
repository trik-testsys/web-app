package trik.testsys.webclient.controller.impl.user.developer

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.impl.user.developer.DeveloperContestController.Companion.CONTEST_ATTR
import trik.testsys.webclient.controller.impl.user.developer.DeveloperContestsController.Companion.CONTESTS_PATH
import trik.testsys.webclient.controller.impl.user.developer.DeveloperMainController.Companion.DEVELOPER_PAGE
import trik.testsys.webclient.controller.impl.user.developer.DeveloperMainController.Companion.DEVELOPER_PATH
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.controller.user.AbstractWebUserMainController.Companion.LOGIN_PATH
import trik.testsys.webclient.entity.user.impl.Developer
import trik.testsys.webclient.service.entity.user.impl.DeveloperService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.view.impl.ContestCreationView
import trik.testsys.webclient.view.impl.ContestView.Companion.toView
import trik.testsys.webclient.view.impl.DeveloperView
import java.util.*

@Controller
@RequestMapping(CONTESTS_PATH)
class DeveloperContestsController(
    loginData: LoginData
) : AbstractWebUserController<Developer, DeveloperView, DeveloperService>(loginData) {

    override val mainPage = CONTESTS_PAGE

    override val mainPath = CONTESTS_PATH

    override fun Developer.toView(timeZoneId: String?) = DeveloperView(
        id = this.id,
        name = this.name,
        accessToken = this.accessToken,
        lastLoginDate = this.lastLoginDate?.atTimeZone(timeZoneId),
        creationDate = this.creationDate?.atTimeZone(timeZoneId),
        additionalInfo = this.additionalInfo,
        contests = this.contests.map { it.toView(timeZoneId) }.sortedBy { it.id }
    )

    @GetMapping
    fun contestsGet(
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        model.addAttribute(CONTEST_ATTR, ContestCreationView.empty())
        model.addAttribute(WEB_USER_ATTR, webUser.toView(timezone))

        return CONTESTS_PAGE
    }

    companion object {

        const val CONTESTS_PATH = "$DEVELOPER_PATH/contests"
        const val CONTESTS_PAGE = "$DEVELOPER_PAGE/contests"
    }
}