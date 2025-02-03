package trik.testsys.webclient.util.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.validation.support.BindingAwareModelMap
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.service.entity.impl.EmergencyMessageService
import trik.testsys.webclient.service.security.UserValidator

@Aspect
@Component
class WebUserControllerAspect(
    private val emergencyMessageService: EmergencyMessageService,
    private val userValidator: UserValidator
) {

    @Order(2)
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

    @Order(1)
    @Around("target(trik.testsys.webclient.controller.user.AbstractWebUserController)")
    fun log(joinPoint: ProceedingJoinPoint): Any {
        try {
            val target = joinPoint.target as AbstractWebUserController<*, *, *>

            val accessToken = target.loginData.accessToken
            val webUserId = userValidator.validateExistence(accessToken)?.id

            val controllerName = target.javaClass.simpleName
            val methodName = joinPoint.signature.name
            val methodArgNames = target.javaClass.methods.find { it.name == methodName }?.parameters?.map { it.name }
            val methodArgValues = joinPoint.args.mapIndexed { index, arg -> "${methodArgNames?.get(index)}=$arg" }

            logger.info("[ $webUserId: $accessToken ] : Calling $controllerName.$methodName($methodArgValues)")

            val result = joinPoint.proceed()
            val logResult = if (result is ResponseEntity<*>) {
                result.headers.contentDisposition.filename
            } else {
                result.toString()
            }

            logger.info("[ $webUserId: $accessToken ] : Called $controllerName.$methodName($methodArgValues). Result: $logResult")

            return result
        } catch (e: Exception) {
            logger.error("Error in WebUserControllerAspect", e)
            return joinPoint.proceed()
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(WebUserControllerAspect::class.java)
    }
}