package trik.testsys.core.repository.named

import org.springframework.data.repository.NoRepositoryBean
import trik.testsys.core.entity.named.NamedEntity
import trik.testsys.core.repository.EntityRepository

/**
 * Repository interface for named entities. Extends [EntityRepository] with methods:
 *
 * 1. [findByName]
 *
 * @see NamedEntity
 * @see EntityRepository
 * @param E entity class, implements [NamedEntity]
 *
 * @author Roman Shishkin
 * @since 2.0.0
 **/
@NoRepositoryBean
interface NamedEntityRepository<E : NamedEntity> : EntityRepository<E> {

    /**
     * Finds all entities by [NamedEntity.name].
     *
     * @param name name by which entities will be found
     * @return [Collection] with all found entities with [NamedEntity.name] equals to [name]
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun findByName(name: String): Collection<E>
}