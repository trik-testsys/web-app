package trik.testsys.webclient.controller.user

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.core.repository.user.UserRepository
import trik.testsys.core.view.user.UserView
import trik.testsys.webclient.controller.impl.main.LoginController
import trik.testsys.webclient.entity.user.WebUser
import trik.testsys.webclient.service.entity.user.WebUserService
import trik.testsys.webclient.service.entity.user.WebUserService.Companion.isFirstTimeLoggedIn
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.addPopupMessage

abstract class AbstractWebUserMainController<U : WebUser, V : UserView<U>, S : WebUserService<U, out UserRepository<U>>> (
    loginData: LoginData
) : AbstractWebUserController<U, V, S>(loginData) {

    @GetMapping
    open fun mainGet(
        @RequestParam(required = false, name = "Logout") logout: String?,
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        if (logout.checkLogout(redirectAttributes)) {
            loginData.invalidate()
            return "redirect:${LoginController.LOGIN_PATH}"
        }
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        model.addAttribute(WEB_USER_ATTR, webUser.toView(timezone))
        return mainPage
    }

    @GetMapping(LOGIN_PATH)
    open fun loginGet(redirectAttributes: RedirectAttributes): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        if (webUser.isFirstTimeLoggedIn()) {
            redirectAttributes.addPopupMessage(
                "Вы успешно зарегистрировались в системе! \n\n" +
                        "Пожалуйста, сохраните сгенерированный Код-доступа, чтобы не потерять его: \n\n" +
                        webUser.accessToken
            )
        }

        webUser.updateLastLoginDate()
        service.save(webUser)

        return "redirect:$mainPath"
    }

    @PostMapping(UPDATE_PATH)
    open fun updatePost(
        @ModelAttribute(WEB_USER_ATTR) webUserView: V,
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        redirectAttributes: RedirectAttributes
    ): String {
        loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        val updatedWebUser = webUserView.toEntity(timezone)

        if (!service.validateName(updatedWebUser)) {
            redirectAttributes.addPopupMessage("Псевдоним не должен быть пустым, содержать Код–доступа или Код-регистрации. Попробуйте другой вариант.")
            return "redirect:$mainPath"
        }
        if (!service.validateAdditionalInfo(updatedWebUser)) {
            redirectAttributes.addPopupMessage("Дополнительная информация не должна содержать Код-доступа или Код-регистрации. Попробуйте другой вариант.")
            return "redirect:$mainPath"
        }

        service.save(updatedWebUser)

        redirectAttributes.addPopupMessage("Данные успешно изменены.")
        return "redirect:$mainPath"
    }

    companion object {

        const val LOGIN_PATH = "/login"
        const val UPDATE_PATH = "/update"
    }
}