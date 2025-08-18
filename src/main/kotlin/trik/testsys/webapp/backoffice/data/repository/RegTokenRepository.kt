package trik.testsys.webapp.backoffice.data.repository

import org.springframework.stereotype.Repository
import trik.testsys.webapp.backoffice.data.entity.Token
import trik.testsys.webapp.backoffice.data.entity.impl.RegToken

@Repository
interface RegTokenRepository : TokenRepository<RegToken> {

    fun findByValue(value: String?) = super.findByTypeAndValue(Token.Type.REGISTRATION, value)
}