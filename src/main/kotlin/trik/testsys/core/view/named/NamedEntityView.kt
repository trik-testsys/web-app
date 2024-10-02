package trik.testsys.core.view.named

import trik.testsys.core.entity.named.NamedEntity
import trik.testsys.core.view.View

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
interface NamedEntityView<E : NamedEntity> : View<E> {

    val name: String
}