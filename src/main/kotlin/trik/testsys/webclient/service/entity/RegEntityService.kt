package trik.testsys.webclient.service.entity

import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.entity.user.UserEntity
import trik.testsys.core.service.EntityService
import trik.testsys.webclient.entity.RegEntity

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
interface RegEntityService<E : RegEntity, RE : UserEntity> : EntityService<E> {

    fun findByRegToken(regToken: AccessToken): E?

    fun register(regToken: AccessToken, name: String, validationBlock: (RE) -> Boolean): RE?
}