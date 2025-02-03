package trik.testsys.webclient.util.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import trik.testsys.core.controller.TrikRestController
import trik.testsys.webclient.service.entity.impl.ApiKeyService

@Aspect
@Component
class RestControllerAspect(
    private val apiKeyService: ApiKeyService
) {

    @Order(1)
    @Around("target(trik.testsys.core.controller.TrikRestController)")
    fun log(joinPoint: ProceedingJoinPoint): Any {
        try {
            val target = joinPoint.target as TrikRestController

            val controllerName = target.javaClass.simpleName
            val methodName = joinPoint.signature.name
            val methodArgNames = target.javaClass.methods.find { it.name == methodName }?.parameters?.map { it.name }
            val methodArgValues = joinPoint.args.mapIndexed { index, arg -> "${methodArgNames?.get(index)}=$arg" }

            logger.info("Calling $controllerName.$methodName($methodArgValues)")

            val result = joinPoint.proceed() as ResponseEntity<*>
            val logResult = if (result.body != null) {
                val body = result.body
                if (body is ByteArray) {
                    "byte[${body.size}]"
                } else {
                    body.toString()
                }
            } else {
                "null"
            }

            logger.info("Called $controllerName.$methodName($methodArgValues). Result: $logResult")

            return result
        } catch (e: Exception) {
            logger.error("Error in RestControllerAspect", e)
            return joinPoint.proceed()
        }
    }

    @Order(2)
    @Around("target(trik.testsys.core.controller.TrikRestController)")
    fun validateApiKey(joinPoint: ProceedingJoinPoint): Any {
        val apiKey = joinPoint.args.firstOrNull { it is String } as? String ?: return ResponseEntity.badRequest().build<Any>()

        if (!apiKeyService.validate(apiKey)) {
            logger.warn("Invalid API key: $apiKey")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build<Any>()
        }

        return joinPoint.proceed()
    }

    companion object {

        private val logger = LoggerFactory.getLogger(RestControllerAspect::class.java)
    }
}