package trik.testsys.webclient.entity

import trik.testsys.core.entity.Entity
import trik.testsys.core.entity.user.AccessToken

/**
 * Interface for entities that have registration token [regToken].
 *
 * @author Roman Shishkin
 * @since 2.0.0
 **/
interface RegEntity : Entity {

    /**
     * Registration token field.
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    val regToken: AccessToken

    companion object {

        /**
         * Length of registration token.
         *
         * @author Roman Shishkin
         * @since 2.0.0
         */
        const val REG_TOKEN_LENGTH = 50
    }
}