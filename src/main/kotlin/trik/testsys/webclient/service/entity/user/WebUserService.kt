package trik.testsys.webclient.service.entity.user

import trik.testsys.core.repository.user.UserRepository
import trik.testsys.core.service.user.AbstractUserService
import trik.testsys.webclient.entity.user.WebUser

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
abstract class WebUserService<E : WebUser, R : UserRepository<E>> : AbstractUserService<E, R>() {

    companion object {

        /**
         * Checks if the [WebUser] is logging in for the first time.
         *
         * @return `true` if the [WebUser] is logging in for the first time, `false` otherwise.
         * @author Roman Shishkin
         * @since 2.0.0
         **/
        fun WebUser.isFirstTimeLoggedIn() = lastLoginDate == null
    }
}