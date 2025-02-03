package trik.testsys.webclient.controller.impl.user.superuser

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.impl.main.LoginController
import trik.testsys.webclient.controller.impl.user.superuser.SuperUserEmergencyMessagesController.Companion.EMERGENCY_MESSAGES_PAGE
import trik.testsys.webclient.controller.impl.user.superuser.SuperUserEmergencyMessagesController.Companion.EMERGENCY_MESSAGES_PATH
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.entity.user.impl.SuperUser
import trik.testsys.webclient.service.entity.impl.EmergencyMessageService
import trik.testsys.webclient.service.entity.user.impl.SuperUserService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.addPopupMessage
import trik.testsys.webclient.view.impl.EmergencyMessageCreationView
import trik.testsys.webclient.view.impl.SuperUserView
import java.util.*
import javax.servlet.http.HttpServletRequest

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
@Controller
@RequestMapping(SuperUserEmergencyMessageController.EMERGENCY_MESSAGE_PATH)
class SuperUserEmergencyMessageController(
    loginData: LoginData,

    private val emergencyMessageService: EmergencyMessageService
) : AbstractWebUserController<SuperUser, SuperUserView, SuperUserService>(loginData) {

    override val mainPage = EMERGENCY_MESSAGE_PAGE

    override val mainPath = EMERGENCY_MESSAGE_PATH

    override fun SuperUser.toView(timeZoneId: String?) = TODO()

    @PostMapping("/create")
    fun emergencyMessagePost(
        @ModelAttribute(EMERGENCY_MESSAGE_ATTR) emergencyMessageView: EmergencyMessageCreationView,
        timeZone: TimeZone,
        request: HttpServletRequest,
        redirectAttributes: RedirectAttributes
    ): String {
        loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        val emergencyMessage = emergencyMessageView.toEntity()
        emergencyMessageService.save(emergencyMessage)

        redirectAttributes.addPopupMessage("Сообщение '${emergencyMessageView.additionalInfo}' успешно создано для пользователей типа '${emergencyMessageView.userType}'.")

        return "redirect:$EMERGENCY_MESSAGES_PATH"
    }

    @PostMapping("/remove/{id}")
    fun emergencyMessageRemove(
        @PathVariable("id") emergencyMessageId: Long,
        timeZone: TimeZone,
        request: HttpServletRequest,
        redirectAttributes: RedirectAttributes
    ): String {
        loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        if (emergencyMessageService.delete(emergencyMessageId)) {
            redirectAttributes.addPopupMessage("Сообщение с ID $emergencyMessageId успешно удалено.")
        } else {
            redirectAttributes.addPopupMessage("Сообщение с ID $emergencyMessageId не найдено.")
        }

        return "redirect:$EMERGENCY_MESSAGES_PATH"
    }

    companion object {

        const val EMERGENCY_MESSAGE_PATH = "$EMERGENCY_MESSAGES_PATH/emergency-message"
        const val EMERGENCY_MESSAGE_PAGE = "$EMERGENCY_MESSAGES_PAGE/emergency-message"

        const val EMERGENCY_MESSAGE_ATTR = "emergencyMessageObj"
    }
}