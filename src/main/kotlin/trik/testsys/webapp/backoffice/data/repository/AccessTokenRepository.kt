package trik.testsys.webapp.backoffice.data.repository

import org.springframework.stereotype.Repository
import trik.testsys.webapp.backoffice.data.entity.Token
import trik.testsys.webapp.backoffice.data.entity.impl.AccessToken
import trik.testsys.webapp.core.data.repository.EntityRepository

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Repository
interface AccessTokenRepository : EntityRepository<AccessToken> {

    fun findByValueAndType(value: String?, type: Token.Type): AccessToken?

    fun findByValueIn(values: Collection<String>): List<AccessToken>
}