package trik.testsys.webapp.backoffice.data.repository

import org.springframework.data.repository.NoRepositoryBean
import trik.testsys.webapp.backoffice.data.entity.Token
import trik.testsys.webapp.core.data.repository.EntityRepository

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@NoRepositoryBean
interface TokenRepository<T : Token> : EntityRepository<T> {

    fun findByTypeAndValue(type: Token.Type, value: String?): T?
}