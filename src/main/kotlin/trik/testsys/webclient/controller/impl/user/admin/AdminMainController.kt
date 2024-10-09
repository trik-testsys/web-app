package trik.testsys.webclient.controller.impl.user.admin

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import trik.testsys.webclient.controller.user.AbstractWebUserMainController
import trik.testsys.webclient.entity.user.impl.Admin
import trik.testsys.webclient.service.entity.user.impl.AdminService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.view.impl.AdminView
import java.util.*

@Controller
@RequestMapping(AdminMainController.ADMIN_PATH)
class AdminMainController(
    loginData: LoginData
) : AbstractWebUserMainController<Admin, AdminView, AdminService>(loginData) {

    override val mainPath = ADMIN_PATH

    override val mainPage = ADMIN_PAGE

    override fun Admin.toView(timeZone: TimeZone) = AdminView(
        id = this.id,
        name = this.name,
        accessToken = this.accessToken,
        creationDate = this.creationDate?.atTimeZone(timeZone),
        lastLoginDate = this.lastLoginDate?.atTimeZone(timeZone),
        viewer = this.viewer,
        additionalInfo = this.additionalInfo
    )

    companion object {

        const val ADMIN_PATH = "/admin"
        const val ADMIN_PAGE = "admin"
    }
}