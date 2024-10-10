package trik.testsys.webclient.controller.impl.user.superuser

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.impl.main.LoginController
import trik.testsys.webclient.controller.impl.user.superuser.SuperUserEmergencyMessageController.Companion.EMERGENCY_MESSAGE_ATTR
import trik.testsys.webclient.controller.impl.user.superuser.SuperUserMainController.Companion.SUPER_USER_PAGE
import trik.testsys.webclient.controller.impl.user.superuser.SuperUserMainController.Companion.SUPER_USER_PATH
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.entity.user.WebUser
import trik.testsys.webclient.entity.user.impl.SuperUser
import trik.testsys.webclient.service.entity.impl.EmergencyMessageService
import trik.testsys.webclient.service.entity.user.impl.SuperUserService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.view.impl.EmergencyMessageCreationView
import trik.testsys.webclient.view.impl.SuperUserView
import java.util.*

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
@Controller
@RequestMapping(SuperUserEmergencyMessagesController.EMERGENCY_MESSAGES_PATH)
class SuperUserEmergencyMessagesController(
    loginData: LoginData,

    private val emergencyMessageService: EmergencyMessageService
) : AbstractWebUserController<SuperUser, SuperUserView, SuperUserService>(loginData) {

    override val mainPage = EMERGENCY_MESSAGES_PAGE

    override val mainPath = EMERGENCY_MESSAGES_PATH

    override fun SuperUser.toView(timeZone: TimeZone) = SuperUserView(
        id = this.id,
        name = this.name,
        accessToken = this.accessToken,
        creationDate = this.creationDate?.atTimeZone(timeZone),
        lastLoginDate = this.lastLoginDate?.atTimeZone(timeZone),
        additionalInfo = this.additionalInfo,
        emergencyMessages = emergencyMessageService.findAll()
    )

    @GetMapping
    fun emergencyMessagesGet(
        timeZone: TimeZone,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        model.addAttribute(WEB_USER_ATTR, webUser.toView(timeZone))
        model.addAttribute(EMERGENCY_MESSAGE_ATTR, EmergencyMessageCreationView.empty())
        model.addAttribute(USER_TYPES_ATTR, WebUser.UserType.all())

        return EMERGENCY_MESSAGES_PAGE
    }

    companion object {

        const val EMERGENCY_MESSAGES_PATH = "$SUPER_USER_PATH/emergency-messages"
        const val EMERGENCY_MESSAGES_PAGE = "$SUPER_USER_PAGE/emergency-messages"

        const val USER_TYPES_ATTR = "userTypes"
    }
}