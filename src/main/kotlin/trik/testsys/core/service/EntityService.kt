package trik.testsys.core.service

import trik.testsys.core.entity.Entity
import trik.testsys.core.repository.EntityRepository
import java.time.LocalDateTime

/**
 * This is a generic service interface that defines common operations for a service.
 * It works with any type of `Entity`.
 *
 * @param E The type of the entity.
 *
 * @see Entity
 * @see EntityRepository
 *
 * @author Roman Shishkin
 * @since 2.0.0
 */
interface EntityService<E : Entity> {

    //region Save methods

    /**
     * Saves the entity.
     *
     * @param entity The entity to save.
     * @return The saved entity.
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun save(entity: E): E

    /**
     * Saves all entities.
     *
     * @param entities The entities to save.
     * @return The saved entities.
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun saveAll(entities: Iterable<E>): List<E>

    //endregion

    //region Find methods

    /**
     * Finds the entity by its ID.
     *
     * @param id The ID of the entity to find.
     * @return The entity with the given ID. If nothing was found - null.
     *
     * @see Entity.getId
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun find(id: Long?): E?

    /**
     * Finds all entities by their IDs.
     *
     * @param ids The IDs of the entities to find.
     * @return The entities with the given IDs.
     *
     * @see Entity.getId
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun findAll(ids: Iterable<Long>): List<E>

    /**
     * Finds all entities.
     *
     * @return All entities.
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun findAll(): List<E>

    /**
     * Finds entities with creation date after the given date.
     *
     * @param creationDate The date by which entities will be found.
     * @return The entities with creation date after the given date.
     *
     * @see Entity.creationDate
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun findByCreationDateAfter(creationDate: LocalDateTime): List<E>

    /**
     * Finds entities with creation date before the given date.
     *
     * @param creationDate The date by which entities will be found.
     * @return The entities with creation date before the given date.
     *
     * @see Entity.creationDate
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun findByCreationDateBefore(creationDate: LocalDateTime): List<E>

    /**
     * Finds entities with creation date between the given dates.
     *
     * @param creationDateFrom The bottom edge for finding entities.
     * @param creationDateTo The top edge for finding entities.
     * @return The entities with creation date between the given dates.
     *
     * @see Entity.creationDate
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun findByCreationDateBetween(creationDateFrom: LocalDateTime, creationDateTo: LocalDateTime): List<E>

    //endregion

    //region Count methods

    /**
     * Counts all entities.
     *
     * @return The number of entities.
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun count(): Long

    //endregion

    //region Exist methods

    /**
     * Checks if the entity with the given ID exists.
     *
     * @param id The ID of the entity to check.
     * @return `true` if the entity exists, `false` otherwise.
     *
     * @see Entity.getId
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun exists(id: Long): Boolean

    //endregion

    //region Delete methods

    /**
     * Deletes the entity with the given ID.
     *
     * @param id The ID of the entity to delete.
     * @return `true` if the entity was deleted, `false` otherwise.
     *
     * @see Entity.getId
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun delete(id: Long): Boolean

    /**
     * Deletes the entity.
     *
     * @param entity The entity to delete.
     * @return `true` if the entity was deleted, `false` otherwise.
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun delete(entity: E): Boolean

    /**
     * Deletes all entities.
     *
     * @param entities The entities to delete.
     * @return `true` if the entities were deleted, `false` otherwise.
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun deleteAll(entities: Iterable<E>): Boolean

    /**
     * Deletes all entities by their IDs.
     *
     * @param ids The IDs of the entities to delete.
     * @return `true` if the entities were deleted, `false` otherwise.
     *
     * @see Entity.getId
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun deleteAllById(ids: Iterable<Long>): Boolean

    /**
     * Deletes all entities. Use with caution.
     *
     * @return `true` if the entities were deleted, `false` otherwise.
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun deleteAll(): Boolean

    //endregion

    //region Validation methods

    /**
     * Validates the [Entity.additionalInfo] field.
     *
     * @param entity entity to validate
     * @return `true` if the entity is valid, `false` otherwise.
     *
     * @see Entity
     *
     * @since 2.0.0
     */
    fun validateAdditionalInfo(entity: E): Boolean = true

    //endregion
}