package trik.testsys.webclient.service.security

import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.entity.user.WebUser

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
interface UserValidator {

    /**
     * Validates if [WebUser] exists by [accessToken]. Returns existence [WebUser] object, `null` - otherwise.
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun validateExistence(accessToken: AccessToken?): WebUser?
}