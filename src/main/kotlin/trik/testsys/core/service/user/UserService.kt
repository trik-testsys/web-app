package trik.testsys.core.service.user

import trik.testsys.core.entity.user.UserEntity
import trik.testsys.core.repository.user.UserRepository
import trik.testsys.core.service.EntityService
import trik.testsys.core.service.named.NamedEntityService

/**
 * Simple interface for user services extends [NamedEntityService].
 * Contains methods that work with [UserEntity.accessToken]:
 *
 * 1. [findByAccessToken]
 * 2. [findAllByAccessTokenIn]
 *
 * @param E user entity class, implements [UserEntity]
 * @param R user repository class, implements [UserRepository]
 *
 * @see EntityService
 * @see UserEntity
 * @see UserRepository
 *
 * @author Roman Shishkin
 * @since 2.0.0
 */
interface UserService<E : UserEntity, R : UserRepository<E>> : NamedEntityService<E> {

    /**
     * Finds entity by [UserEntity.accessToken].
     *
     * @param accessToken access token by which entity will be found.
     * @return entity with [UserEntity.accessToken] equals to [accessToken]. If nothing was found - `null`
     *
     * @see [UserEntity.accessToken]
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun findByAccessToken(accessToken: String): E?

    /**
     * Find all entities by [UserEntity.accessToken].
     *
     * @param accessTokens access tokens by which entities will be found
     * @return [Collection] with all found entities which [UserEntity.accessToken] contained in [accessTokens]
     *
     * @see [UserEntity.accessToken]
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun findAllByAccessTokenIn(accessTokens: Collection<String>): Collection<E>
}