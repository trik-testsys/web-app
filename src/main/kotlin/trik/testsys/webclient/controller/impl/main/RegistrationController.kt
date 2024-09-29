package trik.testsys.webclient.controller.impl.main

import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.View
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.core.entity.user.UserEntity
import trik.testsys.webclient.service.entity.RegEntityService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.addPopupMessage
import trik.testsys.webclient.util.addSessionActiveInfo
import javax.servlet.http.HttpServletRequest

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
@Controller
@RequestMapping(RegistrationController.REGISTRATION_PATH)
class RegistrationController(
    context: ApplicationContext,
    private val loginData: LoginData
) {

    private val registrationServices = context.getBeansOfType(RegEntityService::class.java).values

    @GetMapping
    fun registrationGet(model: Model): String {
        loginData.accessToken?.let { model.addSessionActiveInfo() }
        return REGISTRATION_PAGE
    }

    @PostMapping
    fun registrationPost(
        @RequestParam(required = true) regToken: String,
        @RequestParam(required = true) name: String,
        redirectAttributes: RedirectAttributes,
        request: HttpServletRequest
    ): String {
        val neededService = registrationServices.firstOrNull {
            it.findByRegToken(regToken) != null
        } ?: run {
            redirectAttributes.addPopupMessage("Некорректный Код-доступа. Попробуйте еще раз.")
            return "redirect:$REGISTRATION_PATH"
        }

        val registeredEntity = neededService.register(regToken, name) { !it.name.contains(regToken) } ?: run {
            redirectAttributes.addPopupMessage("Псевдоним не должен содержать Код-регистрации. Попробуйте другой вариант.")
            return "redirect:$REGISTRATION_PATH"
        }

        redirectAttributes.addAttribute(UserEntity::accessToken.name, registeredEntity.accessToken)
        request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT)

        return "redirect:${LoginController.LOGIN_PATH}"
    }

    companion object {

        internal const val REGISTRATION_PAGE = "registration"
        internal const val REGISTRATION_PATH = "/registration"
    }
}