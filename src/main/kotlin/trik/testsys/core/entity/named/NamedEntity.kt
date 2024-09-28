package trik.testsys.core.entity.named

import trik.testsys.core.entity.Entity

/**
 * Interface for named entities. Extends [Entity] with [name] property.
 *
 * @see Entity
 *
 * @author Roman Shishkin
 * @since 2.0.0
 **/
interface NamedEntity : Entity {

    /**
     * Property which contains name string. Must be initialized.
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    var name: String
}