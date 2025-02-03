package trik.testsys.webclient.controller.impl.user.viewer

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.impl.main.LoginController
import trik.testsys.webclient.controller.impl.user.viewer.ViewerMainController.Companion.VIEWER_PAGE
import trik.testsys.webclient.controller.impl.user.viewer.ViewerMainController.Companion.VIEWER_PATH
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.entity.user.impl.Viewer
import trik.testsys.webclient.service.UserAgentParser
import trik.testsys.webclient.service.entity.user.impl.StudentService
import trik.testsys.webclient.service.entity.user.impl.ViewerService
import trik.testsys.webclient.service.impl.UserAgentParserImpl.Companion.WINDOWS_1251
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.view.impl.AdminViewerView.Companion.toViewerView
import trik.testsys.webclient.view.impl.ViewerView

@Controller
@RequestMapping(ViewerAdminsController.ADMINS_PATH)
class ViewerAdminsController(
    loginData: LoginData,

    private val userAgentParser: UserAgentParser,

    private val studentService: StudentService
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
        @RequestParam("Windows") isWindows: String?, // remove lately
        redirectAttributes: RedirectAttributes,
        model: Model
    ): Any {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        val groups = webUser.admins.map { it.groups }.flatten()
        val exportData = studentService.export(groups)

        val filename = "result_${System.currentTimeMillis()}.csv"
        val contentDisposition = "attachment; filename=$filename"
        //        val charset = userAgentParser.getCharset(userAgent) TODO(commented for later usage)
        val charset = isWindows?.let { WINDOWS_1251 } ?: Charsets.UTF_8
        val bytes = exportData.toByteArray(charset)

        val responseEntity = ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
            .header(HttpHeaders.CONTENT_ENCODING, charset.name())
            .contentType(MediaType.TEXT_PLAIN)
            .body(bytes)

        return responseEntity
    }

    companion object {

        const val ADMINS_PATH = "$VIEWER_PATH/admins"
        const val ADMINS_PAGE = "$VIEWER_PAGE/admins"

        const val ADMINS_ATTR = "admins"
    }
}