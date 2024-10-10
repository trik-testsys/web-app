package trik.testsys.webclient.controller.impl.user.developer

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.impl.user.developer.DeveloperMainController.Companion.DEVELOPER_PAGE
import trik.testsys.webclient.controller.impl.user.developer.DeveloperMainController.Companion.DEVELOPER_PATH
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.controller.user.AbstractWebUserMainController.Companion.LOGIN_PATH
import trik.testsys.webclient.entity.user.impl.Developer
import trik.testsys.webclient.service.entity.user.impl.DeveloperService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.view.impl.DeveloperView
import trik.testsys.webclient.view.impl.TaskFileCreationView
import trik.testsys.webclient.view.impl.TaskFileView.Companion.toView

@Controller
@RequestMapping(DeveloperTaskFilesController.TASK_FILES_PATH)
class DeveloperTaskFilesController(
    loginData: LoginData
) : AbstractWebUserController<Developer, DeveloperView, DeveloperService>(loginData){

    override val mainPage = TASK_FILES_PAGE

    override val mainPath = TASK_FILES_PATH

    override fun Developer.toView(timeZoneId: String?) = DeveloperView(
        id = this.id,
        name = this.name,
        accessToken = this.accessToken,
        lastLoginDate = this.lastLoginDate?.atTimeZone(timeZoneId),
        creationDate = this.creationDate?.atTimeZone(timeZoneId),
        additionalInfo = this.additionalInfo,
        polygons = this.polygons.map { it.toView(timeZoneId) }.sortedBy { it.id },
        exercises = this.exercises.map { it.toView(timeZoneId) }.sortedBy { it.id },
        solutions = this.solutions.map { it.toView(timeZoneId) }.sortedBy { it.id }
    )

    @GetMapping
    fun taskFilesGet(
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        model.addAttribute(TASK_FILE_POLYGON_ATTR, TaskFileCreationView.emptyPolygon())
        model.addAttribute(TASK_FILE_EXERCISE_ATTR, TaskFileCreationView.emptyExercise())
        model.addAttribute(TASK_FILE_SOLUTION_ATTR, TaskFileCreationView.emptySolution())

        model.addAttribute(WEB_USER_ATTR, webUser.toView(timezone))

        return TASK_FILES_PAGE
    }

    companion object {

        const val TASK_FILES_PATH = "$DEVELOPER_PATH/taskFiles"
        const val TASK_FILES_PAGE = "$DEVELOPER_PAGE/taskFiles"

        const val TASK_FILE_POLYGON_ATTR = "polygon"
        const val TASK_FILE_EXERCISE_ATTR = "exercise"
        const val TASK_FILE_SOLUTION_ATTR = "solution"
    }
}