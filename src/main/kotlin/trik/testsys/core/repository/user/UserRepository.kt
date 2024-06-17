package trik.testsys.core.repository.user

import org.springframework.data.repository.NoRepositoryBean
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.entity.user.UserEntity
import trik.testsys.core.repository.Repository

/**
 * Repository interface for [UserEntity] typed entities, extends [Repository].
 * Contains methods that work with [UserEntity.accessToken] and [UserEntity.name]:
 *
 * 1. [findByAccessToken]
 * 2. [findAllByAccessTokenIn]
 * 3. [findByName]
 *
 * @param E user entity class, implements [UserEntity]
 *
 * @see Repository
 * @see UserEntity
 *
 * @author Roman Shishkin
 * @since 2.0.0
 */
@NoRepositoryBean
interface UserRepository<E : UserEntity> : Repository<E> {

    /**
     * Finds entity by [UserEntity.accessToken].
     *
     * @param accessToken access token by which entity will be found.
     * @return entity with [UserEntity.accessToken] equals to [accessToken]. If nothing was found - null
     *
     * @see [UserEntity.accessToken]
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun findByAccessToken(accessToken: AccessToken): E?

    /**
     * Find all entities by [UserEntity.accessToken].
     *
     * @param accessTokens access tokens by which entities will be found
     * @return [Collection] with all found entities which [UserEntity.accessToken] contained in [accessTokens]
     *
     * @see [UserEntity.accessToken]
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun findAllByAccessTokenIn(accessTokens: Collection<AccessToken>): Collection<E>

    /**
     * Finds all entities by [UserEntity.name].
     *
     * @param name name by which entities will be found
     * @return [Collection] with all found entities with [UserEntity.name] equals to [name]
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun findByName(name: String): Collection<E>
}