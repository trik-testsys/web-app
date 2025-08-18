package trik.testsys.core.data.entity.user

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import trik.testsys.core.data.entity.AbstractEntity
import java.time.Instant

/**
 * Base mapped superclass for the user domain model with auditing metadata.
 *
 * - The collection of privileges is persisted via `@ElementCollection` in a table
 *   named `${TABLE_PREFIX}_privileges` joined by `user_id`.
 *
 * @property accessToken Last known access token used to authorize in the system.
 * Must be unique per user. Max length is [ACCESS_TOKEN_MAX_LENGTH].
 * @property name User's display name. Max length is [NAME_MAX_LENGTH].
 * @property lastLoginAt Last successful login timestamp in UTC, if available.
 * @property hasLoggedIn Convenience flag reflecting whether [lastLoginAt] is non-null.
 *
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@MappedSuperclass
abstract class AbstractUserEntity(
    @Column(name = "access_token", length = ACCESS_TOKEN_MAX_LENGTH, unique = true)
    var accessToken: String,
    @Column(name = "name", nullable = false, length = NAME_MAX_LENGTH)
    var name: String
) : AbstractEntity() {

    /**
     * Last successful login timestamp in UTC, if available.
     *
     * @author Roman Shishkin
     * @since %CURRENT_VERSION%
     */
    @Column(name = "last_login_at")
    var lastLoginAt: Instant? = null

    /**
     * Convenience flag reflecting whether [lastLoginAt] is non-null.
     *
     * @author Roman Shishkin
     * @since %CURRENT_VERSION%
     */
    @Column(name = "has_logged_in")
    val hasLoggedIn = lastLoginAt != null

    @Suppress("unused")
    companion object {

        /**
         * Maximum supported length for the access token.
         *
         * @author Roman Shishkin
         * @since %CURRENT_VERSION%
         */
        const val ACCESS_TOKEN_MAX_LENGTH = 255

        /**
         * Maximum supported length for the user name.
         *
         * @author Roman Shishkin
         * @since %CURRENT_VERSION%
         */
        const val NAME_MAX_LENGTH = 255

        const val ACCESS_TOKEN = "accessToken"
        const val NAME = "name"
        const val LAST_LOGIN_AT = "lastLoginAt"
        const val HAS_LOGGED_IN = "hasLoggedIn"
    }
}