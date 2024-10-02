package trik.testsys.webclient.view

import trik.testsys.core.view.named.NamedEntityView
import trik.testsys.webclient.entity.NotedEntity

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
interface NotedEntityView<E : NotedEntity> : NamedEntityView<E> {

    val note: String
}