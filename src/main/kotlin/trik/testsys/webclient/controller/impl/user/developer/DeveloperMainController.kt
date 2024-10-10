package trik.testsys.webclient.controller.impl.user.developer

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import trik.testsys.webclient.controller.user.AbstractWebUserMainController
import trik.testsys.webclient.entity.user.impl.Developer
import trik.testsys.webclient.service.entity.user.impl.DeveloperService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.view.impl.*


@Controller
@RequestMapping(DeveloperMainController.DEVELOPER_PATH)
class DeveloperMainController(
    loginData: LoginData
) : AbstractWebUserMainController<Developer, DeveloperView, DeveloperService>(loginData) {

    override val mainPath = DEVELOPER_PATH

    override val mainPage = DEVELOPER_PAGE

    override fun Developer.toView(timeZoneId: String?) = DeveloperView(
        id = this.id,
        name = this.name,
        accessToken = this.accessToken,
        lastLoginDate = this.lastLoginDate?.atTimeZone(timeZoneId),
        creationDate = this.creationDate?.atTimeZone(timeZoneId),
        additionalInfo = this.additionalInfo
    )

    companion object {

        const val DEVELOPER_PATH = "/developer"
        const val DEVELOPER_PAGE = "developer"
    }
}