package trik.testsys.core.service.view

import trik.testsys.core.entity.Entity
import trik.testsys.core.repository.Repository
import trik.testsys.core.view.View

/**
 * Interface for view services. Contains basic methods to work with views.
 *
 * @param E Entity type. Extends [Entity].
 *
 * @see Entity
 * @see Repository
 * @see View
 *
 * @author Roman Shishkin
 * @since 2.0.0
 */
interface ViewService<E : Entity> {

    /**
     * The repository associated with the service.
     */
    val repository: Repository<E>

    /**
     * Finds view by its entity ID.
     *
     * @param id ID of the entity that view is associated with
     * @return View with ID equals to [id]. If nothing was found - `null`
     *
     * @see Entity.getId
     * @see View.id
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun findViewById(id: Long?): View<E>?

    /**
     * Finds all views by their entity IDs.
     *
     * @param ids IDs of the entities that views are associated with
     * @return [Collection] with all found views that IDs contained in [ids]
     *
     * @see Entity.getId
     * @see View.id
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun findAllViewById(ids: Iterable<Long>): Collection<View<E>>

    /**
     * Finds all views.
     *
     * @return [Collection] with all found views
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun findAllView(): Collection<View<E>>
}