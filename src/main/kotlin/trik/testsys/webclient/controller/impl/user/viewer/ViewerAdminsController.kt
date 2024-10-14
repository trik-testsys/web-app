package trik.testsys.webclient.controller.impl.user.viewer

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.impl.main.LoginController
import trik.testsys.webclient.controller.impl.user.viewer.ViewerMainController.Companion.VIEWER_PAGE
import trik.testsys.webclient.controller.impl.user.viewer.ViewerMainController.Companion.VIEWER_PATH
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.entity.impl.Solution
import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.entity.user.impl.Student
import trik.testsys.webclient.entity.user.impl.Viewer
import trik.testsys.webclient.service.UserAgentParser
import trik.testsys.webclient.service.entity.user.impl.ViewerService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.view.impl.AdminViewerView.Companion.toViewerView
import trik.testsys.webclient.view.impl.ViewerView

@Controller
@RequestMapping(ViewerAdminsController.ADMINS_PATH)
class ViewerAdminsController(
    loginData: LoginData,

    private val userAgentParser: UserAgentParser
) : AbstractWebUserController<Viewer, ViewerView, ViewerService>(loginData) {

    override val mainPath = ADMINS_PATH

    override val mainPage = ADMINS_PAGE

    override fun Viewer.toView(timeZoneId: String?) = TODO()

    @GetMapping
    fun adminsGet(
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        val admins = webUser.admins.map { it.toViewerView(timezone) }.sortedBy { it.id }
        model.addAttribute(ADMINS_ATTR, admins)

        return ADMINS_PAGE
    }

    @GetMapping("/export")
    fun exportAdmins(
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        @RequestHeader("User-Agent") userAgent: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): Any {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        val students = webUser.admins.asSequence()
            .map { it.groups }.flatten()
            .map { it.students }.flatten()
            .toSet()
            .sortedBy { it.id }

        val tasks = students.asSequence()
            .map { it.solutions }.flatten()
            .map { it.task }
            .distinct()
            .toSet()
            .sortedBy { it.id }

        val bestScoresByStudents: Map<Student, List<BestScore>> = students.associateWith { student ->
            tasks.map { task ->
                val fullTaskName = "${task.id}: ${task.name}"

                student.getBestSolutionFor(task)?.let { solution ->
                    BestScore(fullTaskName, solution.score.toString())
                } ?: BestScore.notScored(fullTaskName)
            }
        }

        val csvHeader = listOf("ID Организатора", "Псевдоним Организатора", "ID Группы", "Псевдоним Группы", "ID Участника", "Псевдоним Участника", *tasks.map { "${it.id}: ${it.name}" }.toTypedArray())
            .joinToString(separator = ";")
            .plus("\n")

        val csvData = students.map { student ->
            listOf(
                student.group.admin.id.toString(),
                student.group.admin.name,
                student.group.id.toString(),
                student.group.name,
                student.id.toString(),
                student.name,
                *bestScoresByStudents[student]!!.map { it.score }.toTypedArray()
            )
        }

        val csvDataString = csvData.joinToString(separator = "\n") { it.joinToString(separator = ";") }
        val csv = csvHeader.plus(csvDataString)

        val filename = "result_${System.currentTimeMillis()}.csv"
        val contentDisposition = "attachment; filename=$filename"
        val charset = userAgentParser.getCharset(userAgent)
        val bytes = csv.toByteArray(charset)

        val responseEntity = ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
            .header(HttpHeaders.CONTENT_ENCODING, charset.name())
            .contentType(MediaType.TEXT_PLAIN)
            .body(bytes)

        return responseEntity
    }

    private fun Student.getBestSolutionFor(task: Task): Solution? {
        return solutions
            .filter { it.task.compareNames(task) }
            .maxByOrNull { it.score }
    }

    private data class BestScore(
        val taskName: String,
        val score: String
    ) {

        companion object {

            fun notScored(taskName: String) = BestScore(taskName, "-")
        }
    }

    companion object {

        const val ADMINS_PATH = "$VIEWER_PATH/admins"
        const val ADMINS_PAGE = "$VIEWER_PAGE/admins"

        const val ADMINS_ATTR = "admins"
    }
}