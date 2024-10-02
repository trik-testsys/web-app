package trik.testsys.core.service.named

import trik.testsys.core.entity.named.NamedEntity
import trik.testsys.core.service.EntityService

/**
 * Simple interface for named entity services extends [EntityService]. Contains methods that work with [NamedEntity.name]:
 *
 * 1. [findByName]
 *
 * @param E entity class, implements [NamedEntity]
 *
 * @see EntityService
 * @see NamedEntity
 *
 * @author Roman Shishkin
 * @since 2.0.0
 **/
interface NamedEntityService<E : NamedEntity> : EntityService<E> {

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

    /**
     * Validates the [NamedEntity.name] of the [entity].
     *
     * @param entity entity to validate
     * @return `true` if the [NamedEntity.name] is valid, `false` otherwise.
     *
     * @see NamedEntity.name
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun validateName(entity: E): Boolean
}