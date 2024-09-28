package trik.testsys.core.service.named

import trik.testsys.core.entity.named.AbstractNamedEntity
import trik.testsys.core.repository.named.NamedEntityRepository
import trik.testsys.core.service.AbstractService

/**
 * Abstract implementation of [NamedEntityService] interface. Contains common methods for named entity services.
 *
 * @param E named entity class, extends [AbstractNamedEntity]
 *
 * @see NamedEntityService
 * @see AbstractNamedEntity
 * @see NamedEntityRepository
 *
 * @author Roman Shishkin
 * @since 2.0.0
 **/
abstract class AbstractNamedEntityService<E : AbstractNamedEntity, R : NamedEntityRepository<E>> :
    NamedEntityService<E>,
    AbstractService<E, R>() {

    override fun findByName(name: String): Collection<E> {
        val entity = repository.findByName(name)
        return entity
    }
}