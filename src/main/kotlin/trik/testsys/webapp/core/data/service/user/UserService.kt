package trik.testsys.webapp.core.data.service.user

import trik.testsys.webapp.core.data.entity.user.AbstractUserEntity
import trik.testsys.webapp.core.data.service.EntityService

/**
 * Service contract focused on user entities extending [AbstractUserEntity].
 *
 * Adds user-specific lookup operations on top of the generic [EntityService] API.
 *
 * Type parameters:
 * - [T]: concrete user entity type extending [AbstractUserEntity]
 *
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
interface UserService<T : AbstractUserEntity> : EntityService<T> {

    /**
     * Returns a user by exact access token or null if not found.
     *
     * @author Roman Shishkin
     * @since %CURRENT_VERSION%
     */
    fun findByAccessToken(accessToken: String): T?

    /**
     * Returns users by case-insensitive name match.
     *
     * @author Roman Shishkin
     * @since %CURRENT_VERSION%
     */
    fun findByNameIgnoreCase(name: String): List<T>
}


