package trik.testsys.webclient.service.security.impl

import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.entity.user.WebUser
import trik.testsys.webclient.service.entity.user.WebUserService
import trik.testsys.webclient.service.security.UserValidator

@Service
class UserValidatorImpl(
    context: ApplicationContext
) : UserValidator {

    private val webUserServices = context.getBeansOfType(WebUserService::class.java).values

    override fun validateExistence(accessToken: AccessToken?): WebUser? {
        accessToken ?: return null
        val entity = webUserServices.firstNotNullOfOrNull { it.findByAccessToken(accessToken) }
        return entity
    }
}