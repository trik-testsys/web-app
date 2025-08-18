package trik.testsys.core.data.service.user

import org.springframework.transaction.annotation.Transactional
import trik.testsys.core.data.entity.user.AbstractUserEntity
import trik.testsys.core.data.repository.user.UserEntityRepository
import trik.testsys.core.data.service.AbstractService

/**
 * Abstract base implementation of [UserService] backed by a [UserEntityRepository].
 *
 * Provides user-specific read operations and inherits generic CRUD behavior
 * from [AbstractService].
 *
 * Type parameters:
 * - [E]: user entity type extending [AbstractUserEntity]
 * - [R]: repository type extending [UserEntityRepository] for [E]
 *
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Suppress("unused")
@Transactional(readOnly = true)
abstract class AbstractUserService<E, R> : AbstractService<E, R>(), UserService<E>
        where E : AbstractUserEntity,
              R : UserEntityRepository<E> {

    override fun findByAccessToken(accessToken: String): E? =
        repository.findByAccessToken(accessToken)

    override fun findByNameIgnoreCase(name: String): List<E> =
        repository.findByNameIgnoreCase(name)
}