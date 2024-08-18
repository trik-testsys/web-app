package trik.testsys.core.service

import org.springframework.beans.factory.annotation.Autowired
import trik.testsys.core.entity.AbstractEntity
import trik.testsys.core.repository.Repository
import java.time.LocalDateTime

/**
 * Abstract service class that implements [Service] basic CRUD operations.
 *
 * @param E Entity type. Extends [AbstractEntity].
 * @param R Repository type. Extends [Repository].
 *
 * @see Service
 * @see AbstractEntity
 * @see Repository
 *
 * @author Roman Shishkin
 * @since 2.0.0
 */
abstract class AbstractService<E : AbstractEntity, R : Repository<E>> : Service<E> {

    /**
     * The repository associated with the service.
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    @Autowired
    protected lateinit var repository: R

    //region Save methods

    override fun save(entity: E) = repository.save(entity)

    override fun saveAll(entities: Iterable<E>): Collection<E> = repository.saveAll(entities)

    //endregion

    //region Find methods

    override fun find(id: Long?) = id?.let {
        val entity = repository.findById(id)
        entity.orElse(null)
    }

    override fun findAll(ids: Iterable<Long>): Collection<E> = repository.findAllById(ids)

    override fun findAll(): Collection<E> {
        val entities = repository.findAll()
        return entities
    }

    override fun findByCreationDateAfter(creationDate: LocalDateTime): Collection<E> {
        val entities = repository.findByCreationDateAfter(creationDate)
        return entities
    }

    override fun findByCreationDateBefore(creationDate: LocalDateTime): Collection<E> {
        val entities = repository.findByCreationDateBefore(creationDate)
        return entities
    }

    override fun findByCreationDateBetween(
        creationDateFrom: LocalDateTime,
        creationDateTo: LocalDateTime
    ): Collection<E> {
        val entities = repository.findByCreationDateBetween(creationDateFrom, creationDateTo)
        return entities
    }

    //endregion

    //region Count methods

    override fun count(): Long {
        val count = repository.count()
        return count
    }

    //endregion

    //region Exist methods

    override fun exists(id: Long): Boolean {
        val isExist = repository.existsById(id)
        return isExist
    }

    //endregion

    //region Delete methods

    override fun delete(id: Long): Boolean {
        val isDeleted = tryDeletion { repository.deleteById(id) }
        return isDeleted
    }

    override fun delete(entity: E): Boolean {
        val isDeleted = tryDeletion { repository.delete(entity) }
        return isDeleted
    }

    override fun deleteAll(entities: Iterable<E>): Boolean {
        val isDeleted = tryDeletion { repository.deleteAll(entities) }
        return isDeleted
    }

    override fun deleteAllById(ids: Iterable<Long>): Boolean {
        val isDeleted = tryDeletion { repository.deleteAllById(ids) }
        return isDeleted
    }

    override fun deleteAll(): Boolean {
        val isDeleted = tryDeletion { repository.deleteAll() }
        return isDeleted
    }

    //endregion

    companion object {

        /**
         * Tries to delete entity or entities using [block] function and returns result.
         *
         * @param block Deletion block.
         * @return `true` if deletion was successful and there were no exceptions, `false` otherwise.
         *
         * @author Roman Shishkin
         * @since 2.0.0
         */
        private inline fun tryDeletion(block: () -> Unit): Boolean {
            try {
                block()
                return true
            } catch (e: IllegalArgumentException) {
                return false
            }
        }
    }
}