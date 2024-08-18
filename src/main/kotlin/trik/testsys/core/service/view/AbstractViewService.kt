package trik.testsys.core.service.view

import trik.testsys.core.entity.AbstractEntity
import trik.testsys.core.view.View

/**
 * Abstract implementation of [ViewService] interface. Contains common methods for view services.
 *
 * @param E entity class, extends [AbstractEntity]
 *
 * @see ViewService
 * @see AbstractEntity
 *
 * @author Roman Shishkin
 * @since 2.0.0
 */
abstract class AbstractViewService<E : AbstractEntity> : ViewService<E> {

    override fun findAllView(): Collection<View<E>> {
        val views = repository.findAll().map {
            it.toView()
        }
        return views
    }

    override fun findAllViewById(ids: Iterable<Long>): Collection<View<E>> {
        val views = repository.findAllById(ids).map {
            it.toView()
        }
        return views
    }

    override fun findViewById(id: Long?) = id?.let {
        val view = repository.findById(id).orElse(null)?.toView()
        view
    }

    /**
     * Converts entity to view.
     *
     * @param E Entity to convert.
     * @return View that represents the entity.
     *
     * @see View
     * @see AbstractEntity
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    abstract fun E.toView(): View<E>
}