package trik.testsys.core.entity.user

import trik.testsys.core.entity.AbstractEntity
import java.time.LocalDateTime
import javax.persistence.*

/**
 * Simple abstract user entity class. Describes basic behavior.
 *
 * @see UserEntity
 * @see AbstractEntity
 * @author Roman Shishkin
 * @since 2.0.0
 */
@MappedSuperclass
abstract class AbstractUser(
    @Column(
        nullable = false, unique = false, updatable = true,
        length = NAME_MAX_LEN
    ) override var name: String,

    @Column(
        nullable = false, unique = true, updatable = false,
        length = ACCESS_TOKEN_MAX_LEN
    ) override var accessToken: AccessToken
) : UserEntity, AbstractEntity() {

    @Column(nullable = true, unique = false, updatable = true)
    final override var lastLoginDate: LocalDateTime? = null

    override fun updateLastLoginDate() {
        lastLoginDate = LocalDateTime.now(DEFAULT_ZONE_ID)
    }

    companion object {

        private const val NAME_MAX_LEN = 127
        private const val ACCESS_TOKEN_MAX_LEN = 255

        /**
         * Anonymous user entity for system usage.
         *
         * @author Roman Shishkin
         * @since 2.0.0
         */
        object System : AbstractUser(
            name = "System User",
            accessToken = "system-user-non-accessible-token"
        )
    }
}