package trik.testsys.webclient.controller.impl.user.superuser

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import trik.testsys.webclient.controller.user.AbstractWebUserMainController
import trik.testsys.webclient.entity.user.impl.SuperUser
import trik.testsys.webclient.service.entity.user.impl.SuperUserService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.view.impl.SuperUserView
import java.util.*

@Controller
@RequestMapping(SuperUserMainController.SUPER_USER_PATH)
class SuperUserMainController(
    loginData: LoginData
) : AbstractWebUserMainController<SuperUser, SuperUserView, SuperUserService>(loginData) {

    override val mainPath = SUPER_USER_PATH

    override val mainPage = SUPER_USER_PAGE

    override fun SuperUser.toView(timeZone: TimeZone) = SuperUserView(
        id = this.id,
        name = this.name,
        accessToken = this.accessToken,
        creationDate = this.creationDate?.atTimeZone(timeZone),
        lastLoginDate = this.lastLoginDate?.atTimeZone(timeZone),
        additionalInfo = this.additionalInfo
    )

    companion object {

        const val SUPER_USER_PATH = "/superuser"
        const val SUPER_USER_PAGE = "superuser"
    }
}