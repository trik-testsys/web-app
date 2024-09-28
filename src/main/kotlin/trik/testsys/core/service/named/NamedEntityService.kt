package trik.testsys.core.service.named

import trik.testsys.core.entity.named.NamedEntity
import trik.testsys.core.service.Service

/**
 * Simple interface for named entity services extends [Service]. Contains methods that work with [NamedEntity.name]:
 *
 * 1. [findByName]
 *
 * @param E entity class, implements [NamedEntity]
 *
 * @see Service
 * @see NamedEntity
 *
 * @author Roman Shishkin
 * @since 2.0.0
 **/
interface NamedEntityService<E : NamedEntity> : Service<E> {

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