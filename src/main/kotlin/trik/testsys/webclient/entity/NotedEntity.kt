package trik.testsys.webclient.entity

import trik.testsys.core.entity.named.NamedEntity


/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
interface NotedEntity : NamedEntity {

    var note: String
}