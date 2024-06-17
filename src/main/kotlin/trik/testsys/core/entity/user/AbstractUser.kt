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
abstract class AbstractUser : UserEntity, AbstractEntity() {

    @Column(
        nullable = false, unique = false, length = NAME_MAX_LEN,
        columnDefinition = "VARCHAR($NAME_MAX_LEN) DEFAULT '$DEFAULT_NAME'"
    ) override var name: String = DEFAULT_NAME

    @Column(
        nullable = false, unique = true, length = ACCESS_TOKEN_MAX_LEN,
        columnDefinition = "VARCHAR($ACCESS_TOKEN_MAX_LEN) DEFAULT '$DEFAULT_ACCESS_TOKEN'"
    ) override var accessToken = DEFAULT_ACCESS_TOKEN

    companion object {

        private const val DEFAULT_NAME = ""
        private const val DEFAULT_ACCESS_TOKEN = ""

        private const val NAME_MAX_LEN = 128
        private const val ACCESS_TOKEN_MAX_LEN = 256

        /**
         * Anonymous user entity for system usage.
         *
         * @author Roman Shishkin
         * @since 2.0.0
         */
        val System = object: AbstractUser() {

            override var name = "System User"

            override var accessToken = "system-user-non-accessible-token"

            override val creationDate: LocalDateTime = LocalDateTime.MIN
        }
    }
}