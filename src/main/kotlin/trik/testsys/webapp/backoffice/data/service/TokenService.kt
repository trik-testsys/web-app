package trik.testsys.webapp.backoffice.data.service

import trik.testsys.webapp.backoffice.data.entity.Token
import trik.testsys.webapp.core.data.service.EntityService

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
interface TokenService<T : Token> : EntityService<T> {

    fun generate(): T

    fun findByValue(value: String): T?
}