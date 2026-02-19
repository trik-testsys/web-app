package trik.testsys.webapp.backoffice.service

import trik.testsys.webapp.backoffice.data.entity.impl.User

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
interface UserEmailService {

    fun sendVerificationToken(user: User, newEmail: String?)

    fun verify(user: User, verificationToken: String): Boolean

    fun sendAccessToken(email: String): Boolean
}