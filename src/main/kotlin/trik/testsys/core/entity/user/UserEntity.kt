package trik.testsys.core.entity.user

import trik.testsys.core.entity.Entity

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
interface UserEntity : Entity {

    /**
     * Property which contains user name string. Must be initialized.
     * @author Roman Shishkin
     * @since 2.0.0
     */
    val name: String

    /**
     * Property which contains unique access token. It is used for identify user in system. Must be initialized.
     * @author Roman Shishkin
     * @since 2.0.0
     */
    val accessToken: String
}