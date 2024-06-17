package trik.testsys.core.entity.user

import trik.testsys.core.entity.Entity

typealias AccessToken = String

/**
 * Simple interface for user entities. Extends [Entity] with [name] and [accessToken] properties.
 * In fact every entity that implements this can be identified by [accessToken].
 *
 * @see Entity
 * @author Roman Shishkin
 * @since 2.0.0
 */
interface UserEntity : Entity {

    /**
     * Property which contains user name string. Must be initialized.
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    val name: String

    /**
     * Property which contains unique access token.
     * It is used to identify user in a system.
     * Must be initialized.
     * In fact should be an entity identifier.
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    val accessToken: AccessToken
}