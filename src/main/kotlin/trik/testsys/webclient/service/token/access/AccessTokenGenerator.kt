package trik.testsys.webclient.service.token.access

import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.entity.user.UserEntity
import trik.testsys.webclient.service.token.TokenGenerator

/**
 * Interface for [UserEntity.accessToken] generators.
 *
 * @author Roman Shishkin
 * @since 2.0.0
 */
interface AccessTokenGenerator : TokenGenerator {


    /**
     * Generates [UserEntity.accessToken] by [UserEntity.name].
     *
     * @param string [UserEntity.name]
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    override fun generate(string: String): AccessToken
}