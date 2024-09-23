package trik.testsys.core.entity.user

import trik.testsys.core.entity.Entity
import java.time.LocalDateTime

typealias AccessToken = String

/**
 * Simple interface for user entities. Extends [Entity] with [name] and [accessToken] properties.
 * In fact every entity that implements this can be identified by [accessToken].
 *
 * @see Entity
 * @author Roman Shishkin
 * @since 2.0.0
 */
interface UserEntity : Entity {

    /**
     * Property which contains user name string. Must be initialized.
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    var name: String

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
    var lastLoginDate: LocalDateTime

    /**
     * Update [lastLoginDate] field. Should be used when used logging in.
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun updateLastLoginDate()
}