package trik.testsys.webapp.backoffice.data.repository

import org.springframework.stereotype.Repository
import trik.testsys.webapp.backoffice.data.entity.Token
import trik.testsys.webapp.backoffice.data.entity.impl.AccessToken

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Repository
interface AccessTokenRepository : TokenRepository<AccessToken> {

    fun findByValue(value: String?) = super.findByTypeAndValue(Token.Type.ACCESS, value)
}