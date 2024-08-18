package trik.testsys.core.view

import trik.testsys.core.entity.Entity

abstract class AbstractView<E : Entity>(entity: E) : View<E> {

    override val id: Long? = entity.id
}