package trik.testsys.webapp.backoffice.data.repository

import org.springframework.stereotype.Repository
import trik.testsys.webapp.backoffice.data.entity.impl.RegToken
import trik.testsys.webapp.core.data.repository.EntityRepository

@Repository
interface RegTokenRepository : EntityRepository<RegToken> {

    fun findByValue(value: String?): RegToken?
}