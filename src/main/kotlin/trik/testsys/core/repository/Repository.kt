package trik.testsys.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import trik.testsys.core.entity.Entity
import java.time.LocalDateTime

/**
 * Simple repository interface for entities, extends [JpaRepository].
 * Contains methods that works with [Entity.creationDate]:
 *
 * 1. [findByCreationDateAfter]
 * 2. [findByCreationDateBefore]
 * 3. [findByCreationDateBetween]
 *
 * @see JpaRepository
 * @param E entity class, implements [Entity]
 * @author Roman Shishkin
 * @since 2.0.0
 */
@NoRepositoryBean
interface Repository<E : Entity> : JpaRepository<E, Long> {

    /**
     * Finds entities with [Entity.creationDate] greater than [creationDate].
     *
     * @param creationDate date by which entities will be found
     * @return [Collection] of entities with [Entity.creationDate] greater than [creationDate]
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun findByCreationDateAfter(creationDate: LocalDateTime): Collection<E>

    /**
     * Finds entities with [Entity.creationDate] less than [creationDate].
     *
     * @param creationDate date by which entities will be found
     * @return [Collection] of entities with [Entity.creationDate] less than [creationDate]
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun findByCreationDateBefore(creationDate: LocalDateTime): Collection<E>

    /**
     * Finds entities with [Entity.creationDate] greater than [creationDateFrom] and less than [creationDateTo].
     *
     * @param creationDateFrom bottom edge for finding entities
     * @param creationDateTo top edge for finding entities
     * @return [Collection] of entities with [Entity.creationDate] between [creationDateFrom] and [creationDateTo]
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun findByCreationDateBetween(creationDateFrom: LocalDateTime, creationDateTo: LocalDateTime): Collection<E>
}