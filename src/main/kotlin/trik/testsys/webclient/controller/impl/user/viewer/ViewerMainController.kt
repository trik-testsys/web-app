package trik.testsys.webclient.controller.impl.user.viewer

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import trik.testsys.webclient.controller.user.AbstractWebUserMainController
import trik.testsys.webclient.entity.user.impl.Viewer
import trik.testsys.webclient.service.entity.user.impl.ViewerService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.view.impl.ViewerView
import java.util.*


@Controller
@RequestMapping(ViewerMainController.VIEWER_PATH)
class ViewerMainController(
    loginData: LoginData
) : AbstractWebUserMainController<Viewer, ViewerView, ViewerService>(loginData) {

    override val mainPath = VIEWER_PATH

    override val mainPage = VIEWER_PAGE

    override fun Viewer.toView(timeZone: TimeZone) = ViewerView(
        id = this.id,
        name = this.name,
        accessToken = this.accessToken,
        lastLoginDate = this.lastLoginDate?.atTimeZone(timeZone),
        creationDate = this.creationDate?.atTimeZone(timeZone),
        regToken = this.regToken,
        additionalInfo = this.additionalInfo
    )

    companion object {

        const val VIEWER_PATH = "/viewer"

        const val VIEWER_PAGE = "viewer"
    }
}

//        val headers = HttpHeaders()
//        headers.contentType = MediaType.APPLICATION_OCTET_STREAM
//        headers.contentDisposition = ContentDisposition.builder("attachment")
//            .filename("results-${LocalDateTime.now(UTC).plusHours(3)}.csv")
//            .build()
//
//        headers.acceptLanguage = Locale.LanguageRange.parse("ru-RU, en-US")
//        headers.acceptCharset = listOf(Charsets.UTF_8, Charsets.ISO_8859_1, Charsets.US_ASCII)
//
//        headers.acceptCharset.add(Charset.forName("windows-1251"))
//        headers.acceptCharset.add(Charset.forName("windows-1252"))
//        headers.acceptCharset.add(Charset.forName("windows-1254"))
//        headers.acceptCharset.add(Charset.forName("windows-1257"))
//        headers.acceptCharset.add(Charset.forName("windows-1258"))
//        headers.acceptCharset.add(Charset.forName("windows-874"))
//        headers.acceptCharset.add(Charset.forName("windows-949"))
//        headers.acceptCharset.add(Charset.forName("windows-950"))
//        headers.acceptCharset.add(Charset.forName("ANSI_X3.4-1968"))