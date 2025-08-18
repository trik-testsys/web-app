package trik.testsys.core.data.repository.user

import org.springframework.data.repository.NoRepositoryBean
import trik.testsys.core.data.entity.user.AbstractUserEntity
import trik.testsys.core.data.repository.EntityRepository

/**
 * Repository contract for accessing user entities that extend [AbstractUserEntity].
 *
 * This interface purposefully avoids binding to a specific `@Entity` so that
 * downstream applications can expose their concrete implementation while
 * reusing the common repository API.
 *
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@NoRepositoryBean
interface UserEntityRepository<T : AbstractUserEntity> : EntityRepository<T> {

    /**
     * Finds a user by an exact access token match.
     *
     * @author Roman Shishkin
     * @since %CURRENT_VERSION%
     */
    fun findByAccessToken(accessToken: String): T?

    /**
     * Case-insensitive search by user name.
     *
     * @author Roman Shishkin
     * @since %CURRENT_VERSION%
     */
    fun findByNameIgnoreCase(name: String): List<T>
}


