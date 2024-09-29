package trik.testsys.core.service.user

import trik.testsys.core.entity.user.AbstractUser
import trik.testsys.core.repository.user.UserRepository
import trik.testsys.core.service.AbstractService
import trik.testsys.core.service.named.AbstractNamedEntityService

/**
 * Abstract implementation of [UserService] interface. Contains common methods for user services.
 *
 * @param E user entity class, extends [AbstractUser]
 * @param R user repository class, extends [UserRepository]
 *
 * @see UserService
 * @see AbstractUser
 * @see UserRepository
 * @see AbstractNamedEntityService
 *
 * @author Roman Shishkin
 * @since 2.0.0
 */
abstract class AbstractUserService<E : AbstractUser, R : UserRepository<E>> :
    UserService<E>,
    AbstractNamedEntityService<E, R>() {

    override fun findByAccessToken(accessToken: String): E? {
        val entity = repository.findByAccessToken(accessToken)
        return entity
    }

    override fun findAllByAccessTokenIn(accessTokens: Collection<String>): Collection<E> {
        val entities = repository.findAllByAccessTokenIn(accessTokens)
        return entities
    }
}