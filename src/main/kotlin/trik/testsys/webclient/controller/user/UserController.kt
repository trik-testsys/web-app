package trik.testsys.webclient.controller.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.core.repository.user.UserRepository
import trik.testsys.core.utils.marker.TrikController
import trik.testsys.core.view.user.UserView
import trik.testsys.webclient.controller.impl.main.LoginController
import trik.testsys.webclient.entity.user.WebUser
import trik.testsys.webclient.service.entity.user.WebUserService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.addExitMessage
import trik.testsys.webclient.util.addPopupMessage
import trik.testsys.webclient.util.addSessionExpiredMessage
import java.util.TimeZone

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
abstract class UserController<U : WebUser, V : UserView<U>, S : WebUserService<U, out UserRepository<U>>>(
    private val loginData: LoginData
) : TrikController {

    protected abstract val MAIN_PATH: String

    protected abstract val MAIN_PAGE: String

    @Autowired
    protected lateinit var service: S

    @GetMapping
    open fun mainGet(
        @RequestParam(required = false, name = "Logout") logout: String?,
        timeZone: TimeZone,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        if (logout.checkLogout(redirectAttributes)) {
            loginData.invalidate()
            return "redirect:${LoginController.LOGIN_PATH}"
        }
        val webUser = validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        model.addAttribute(WEB_USER, webUser.toView(timeZone))
        return MAIN_PAGE
    }

    @GetMapping(LOGIN_PATH)
    open fun loginGet(redirectAttributes: RedirectAttributes): String {
        val webUser = validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        if (service.firstTimeCheck(webUser)) {
            redirectAttributes.addPopupMessage(
                "Вы успешно зарегистрировались в системе! \n\n" +
                "Пожалуйста, сохраните сгенерированный Код-доступа, чтобы не потерять его: \n\n" +
                webUser.accessToken
            )
        }

        webUser.updateLastLoginDate()
        service.save(webUser)

        return "redirect:$MAIN_PATH"
    }

    @PostMapping(UPDATE_PATH)
    open fun updatePost(
        @ModelAttribute(WEB_USER) webUserView: V,
        timeZone: TimeZone,
        redirectAttributes: RedirectAttributes
    ): String {
        validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        val updatedWebUser = webUserView.toEntity(timeZone)
        if (!service.validateName(updatedWebUser)) {
            redirectAttributes.addPopupMessage("Псевдоним не должен быть пустым или совпадать с Кодом-доступа. Попробуйте другой вариант.")
            return "redirect:$MAIN_PATH"
        }

        service.save(updatedWebUser)

        redirectAttributes.addPopupMessage("Данные успешно изменены.")
        return "redirect:$MAIN_PATH"
    }

    protected abstract fun U.toView(timeZone: TimeZone): V

    /**
     * @author Roman Shishkin
     * @since 2.0.0
     **/
    protected fun validate(redirectAttributes: RedirectAttributes): U? {
        val webUser = loginData.accessToken?.let { service.findByAccessToken(it) } ?: run {
            loginData.invalidate()
            redirectAttributes.addSessionExpiredMessage()
            return null
        }

        return webUser
    }

    /**
     * @author Roman Shishkin
     * @since 2.0.0
     **/
    protected fun String?.checkLogout(redirectAttributes: RedirectAttributes): Boolean {
        if (this != null) {
            redirectAttributes.addExitMessage()
            return true
        }

        return false
    }

    companion object {

        const val LOGIN_PATH = "/login"
        const val UPDATE_PATH = "/update"

        const val WEB_USER = "webUser"
    }
}