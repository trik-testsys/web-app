package trik.testsys.webclient.repository

import org.springframework.data.repository.NoRepositoryBean
import trik.testsys.core.repository.EntityRepository
import trik.testsys.webclient.entity.RegEntity

/**
 * Repository for [RegEntity] entities. Extends [EntityRepository] with methods working with registration token:
 *
 * 1. [findByRegToken] – Find entity by registration token.
 *
 * @author Roman Shishkin
 * @since 2.0.0
 **/
@NoRepositoryBean
interface RegEntityRepository<E : RegEntity> : EntityRepository<E> {

    /**
     * Find entity by [RegEntity.regToken].
     *
     * @param regToken Registration token.
     * @return Entity with given registration token or `null` if not found.
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun findByRegToken(regToken: String): E?
}