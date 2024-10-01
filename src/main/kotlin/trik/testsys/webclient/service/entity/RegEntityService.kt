package trik.testsys.webclient.service.entity

import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.entity.user.UserEntity
import trik.testsys.core.service.named.NamedEntityService
import trik.testsys.webclient.entity.RegEntity

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
interface RegEntityService<E : RegEntity, RE : UserEntity> : NamedEntityService<E> {

    fun findByRegToken(regToken: AccessToken): E?

    fun register(regToken: AccessToken, name: String): RE?

    override fun validateName(entity: E) = !entity.name.containsRegToken(entity.regToken)

    override fun validateAdditionalInfo(entity: E) = !entity.additionalInfo.containsRegToken(entity.regToken)

    companion object {

        fun String.containsRegToken(regToken: AccessToken) = contains(regToken, ignoreCase = true)
    }
}