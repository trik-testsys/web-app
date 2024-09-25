package trik.testsys.webclient.service.entity.user

import trik.testsys.core.repository.user.UserRepository
import trik.testsys.core.service.user.AbstractUserService
import trik.testsys.webclient.entity.user.WebUser

/**
 * @author Roman Shishkin
 * @since 2.0.0
**/
abstract class WebUserService<E : WebUser, R : UserRepository<E>> : AbstractUserService<E, R>() {

    /**
     * Checks if the [entity] is logging in for the first time.
     *
     * @return `true` if the [entity] is logging in for the first time, `false` otherwise.
     * @author Roman Shishkin
     * @since 2.0.0
     **/
    fun firstTimeCheck(entity: E) = entity.lastLoginDate.isEqual(entity.creationDate)

    /**
     * Validates the [WebUser.name] of the [entity]. The name should not be empty or equals to [WebUser.accessToken].
     *
     *
     * @return `true` if the [WebUser.name] is valid, `false` otherwise.
     * @author Roman Shishkin
     * @since 2.0.0
     **/
    fun validateName(entity: E) = entity.name.isNotEmpty() && entity.name != entity.accessToken
}