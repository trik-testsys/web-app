package trik.testsys.core.entity.user

import trik.testsys.core.entity.Entity
import trik.testsys.core.entity.named.NamedEntity
import java.time.LocalDateTime

typealias AccessToken = String

/**
 * Simple interface for user entities. Extends [Entity] with [accessToken] properties.
 * In fact every entity that implements this can be identified by [accessToken].
 *
 * @see Entity
 * @author Roman Shishkin
 * @since 2.0.0
 */
interface UserEntity : NamedEntity {

    /**
     * Property which contains unique access token.
     * It is used to identify user in a system.
     * Must be initialized.
     * In fact should be an entity identifier.
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    var accessToken: AccessToken

    /**
     * Property which contains date time of last user login.
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    var lastLoginDate: LocalDateTime?

    /**
     * Update [lastLoginDate] field. Should be used when a user is logging in.
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun updateLastLoginDate()
}