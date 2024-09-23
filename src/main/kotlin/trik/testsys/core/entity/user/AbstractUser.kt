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
        nullable = false, unique = false, length = NAME_MAX_LEN,
        columnDefinition = "VARCHAR($NAME_MAX_LEN)"
    ) override var name: String,

    @Column(
        nullable = false, unique = true, length = ACCESS_TOKEN_MAX_LEN,
        columnDefinition = "VARCHAR($ACCESS_TOKEN_MAX_LEN)"
    ) override var accessToken: AccessToken
) : UserEntity, AbstractEntity() {

    @Column(nullable = false, unique = false, columnDefinition = "DATETIME")
    override var lastLoginDate: LocalDateTime = creationDate

    override fun updateLastLoginDate() {
        lastLoginDate = LocalDateTime.now(DEFAULT_ZONE)
    }

    companion object {

        private const val NAME_MAX_LEN = 128
        private const val ACCESS_TOKEN_MAX_LEN = 256

        /**
         * Anonymous user entity for system usage.
         *
         * @author Roman Shishkin
         * @since 2.0.0
         */
        object System : AbstractUser(
            name = "System User",
            accessToken = "system-user-non-accessible-token"
        ) {

            override val creationDate: LocalDateTime = LocalDateTime.MIN

            override var lastLoginDate: LocalDateTime = LocalDateTime.MAX
        }
    }
}