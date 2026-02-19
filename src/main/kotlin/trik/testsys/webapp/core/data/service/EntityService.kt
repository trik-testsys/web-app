package trik.testsys.webapp.core.data.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import trik.testsys.webapp.core.data.entity.AbstractEntity

/**
 * Base service contract for entities extending [AbstractEntity].
 *
 * Provides a thin abstraction over Spring Data repositories to allow
 * business-layer extensions and shared behaviors.
 *
 * Type parameters:
 * - [E]: entity type extending [AbstractEntity]
 *
 * @author Roman Shishkin
 * @since 3.12.0
 */
interface EntityService<E : AbstractEntity> {

    /**
     * Returns the existing entity by id or throws [NoSuchElementException] if not found.
     *
     * @author Roman Shishkin
     * @since 3.12.0
     */
    fun getById(id: Long): E

    /**
     * Returns the entity by id or null if it does not exist.
     *
     * @author Roman Shishkin
     * @since 3.12.0
     */
    fun findById(id: Long): E?

    /**
     * Checks whether an entity with the given id exists.
     *
     * @author Roman Shishkin
     * @since 3.12.0
     */
    fun existsById(id: Long): Boolean

    /**
     * Returns all entities.
     *
     * @author Roman Shishkin
     * @since 3.12.0
     */
    fun findAll(): List<E>

    /**
     * Returns all entities matching the given ids.
     *
     * @author Roman Shishkin
     * @since 3.12.0
     */
    fun findAllById(ids: Iterable<Long>): List<E>

    /**
     * Returns all entities matching the optional JPA [Specification].
     *
     * @author Roman Shishkin
     * @since 3.12.0
     */
    fun findAll(specification: Specification<E>?): List<E>

    /**
     * Returns a page of entities matching the optional JPA [Specification] and [Pageable].
     *
     * @author Roman Shishkin
     * @since 3.12.0
     */
    fun findAll(specification: Specification<E>?, pageable: Pageable): Page<E>

    /**
     * Returns the total number of entities.
     *
     * @author Roman Shishkin
     * @since 3.12.0
     */
    fun count(): Long

    /**
     * Saves the given entity and returns the persisted instance.
     *
     * @author Roman Shishkin
     * @since 3.12.0
     */
    fun save(entity: E): E

    /**
     * Saves all the given entities and returns persisted instances.
     *
     * @author Roman Shishkin
     * @since 3.12.0
     */
    fun saveAll(entities: Iterable<E>): List<E>

    /**
     * Deletes an entity by its id.
     *
     * @author Roman Shishkin
     * @since 3.12.0
     */
    fun deleteById(id: Long)

    /**
     * Deletes an entity.
     *
     * @author Roman Shishkin
     * @since 3.12.0
     */
    fun delete(entity: E)
}


