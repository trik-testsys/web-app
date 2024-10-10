package trik.testsys.webclient.controller.impl.user.developer

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.impl.user.developer.DeveloperMainController.Companion.DEVELOPER_PAGE
import trik.testsys.webclient.controller.impl.user.developer.DeveloperMainController.Companion.DEVELOPER_PATH
import trik.testsys.webclient.controller.impl.user.developer.DeveloperTaskController.Companion.TASK_ATTR
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.controller.user.AbstractWebUserMainController.Companion.LOGIN_PATH
import trik.testsys.webclient.entity.user.impl.Developer
import trik.testsys.webclient.service.entity.user.impl.DeveloperService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.view.impl.ContestCreationView
import trik.testsys.webclient.view.impl.DeveloperView
import trik.testsys.webclient.view.impl.TaskView.Companion.toView
import java.util.*

@Controller
@RequestMapping(DeveloperTasksController.TASKS_PATH)
class DeveloperTasksController(
    loginData: LoginData
) : AbstractWebUserController<Developer, DeveloperView, DeveloperService>(loginData) {

    override val mainPage = TASKS_PAGE

    override val mainPath = TASKS_PATH

    override fun Developer.toView(timeZoneId: String?) = DeveloperView(
        id = this.id,
        name = this.name,
        accessToken = this.accessToken,
        creationDate = this.creationDate?.atTimeZone(timeZoneId),
        lastLoginDate = this.lastLoginDate?.atTimeZone(timeZoneId),
        additionalInfo = this.additionalInfo,
        tasks = this.tasks.map { it.toView(timeZoneId) }.sortedBy { it.id }
    )

    @GetMapping
    fun tasksGet(
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        model.addAttribute(TASK_ATTR, ContestCreationView.empty())
        model.addAttribute(WEB_USER_ATTR, webUser.toView(timezone))

        return TASKS_PAGE
    }

    companion object {

        const val TASKS_PATH = "$DEVELOPER_PATH/tasks"
        const val TASKS_PAGE = "$DEVELOPER_PAGE/tasks"
    }
}