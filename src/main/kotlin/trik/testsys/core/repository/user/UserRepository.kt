package trik.testsys.core.repository.user

import org.springframework.data.repository.NoRepositoryBean
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.entity.user.UserEntity
import trik.testsys.core.repository.named.NamedEntityRepository

/**
 * Repository interface for [UserEntity] typed entities, extends [NamedEntityRepository].
 * Contains methods that work with [UserEntity.accessToken].
 *
 * 1. [findByAccessToken]
 * 2. [findAllByAccessTokenIn]
 * 3. [findByNameAndAccessToken]
 *
 * @param E user entity class, implements [UserEntity]
 *
 * @see NamedEntityRepository
 * @see UserEntity
 *
 * @author Roman Shishkin
 * @since 2.0.0
 */
@NoRepositoryBean
interface UserRepository<E : UserEntity> : NamedEntityRepository<E> {

    /**
     * Finds entity by [UserEntity.accessToken].
     *
     * @param accessToken access token by which entity will be found.
     * @return entity with [UserEntity.accessToken] equals to [accessToken]. If nothing was found - `null`
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
    fun findAllByAccessTokenIn(accessTokens: Collection<AccessToken>): List<E>

    /**
     * Finds entity by [UserEntity.name] and [UserEntity.accessToken].
     *
     * @param name name by which entity will be found
     * @param accessToken access token by which entity will be found
     * @return entity with [UserEntity.name] equals to [name] and [UserEntity.accessToken] equals to [accessToken]. If nothing was found - `null`
     *
     * @see [UserEntity.name]
     * @see [UserEntity.accessToken]
     * @since 2.0.0
     */
    fun findByNameAndAccessToken(name: String, accessToken: AccessToken): E?
}