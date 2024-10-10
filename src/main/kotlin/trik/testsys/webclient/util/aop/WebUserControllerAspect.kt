package trik.testsys.webclient.util.aop

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component
import org.springframework.ui.Model
import org.springframework.validation.support.BindingAwareModelMap
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.service.entity.impl.EmergencyMessageService
import trik.testsys.webclient.service.security.UserValidator
import trik.testsys.webclient.util.addSessionActiveInfo

@Aspect
@Component
class WebUserControllerAspect(
    private val emergencyMessageService: EmergencyMessageService,
    private val userValidator: UserValidator
) {

    @Around("target(trik.testsys.webclient.controller.user.AbstractWebUserController)")
    fun addEmergencyMessage(joinPoint: ProceedingJoinPoint): Any {
        val target = joinPoint.target as AbstractWebUserController<*, *, *>
        val redirectAttributes = joinPoint.args.firstOrNull { it is RedirectAttributes } as? RedirectAttributes
        val model = joinPoint.args.firstOrNull { it is BindingAwareModelMap } as? BindingAwareModelMap

        val accessToken = target.loginData.accessToken ?: run {
            return joinPoint.proceed()
        }

        val webUser = userValidator.validateExistence(accessToken) ?: run {
            return joinPoint.proceed()
        }

        val emergencyMessage = emergencyMessageService.findByUserType(webUser.type) ?: run {
            return joinPoint.proceed()
        }

        redirectAttributes?.addFlashAttribute("emergencyMessage", emergencyMessage.additionalInfo)
        model?.addAttribute("emergencyMessage", emergencyMessage.additionalInfo)

        return joinPoint.proceed()
    }
}