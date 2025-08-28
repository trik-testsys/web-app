package trik.testsys.webapp.backoffice.data.repository

import org.springframework.stereotype.Repository
import trik.testsys.webapp.backoffice.data.entity.impl.StudentGroupToken
import trik.testsys.webapp.core.data.repository.EntityRepository

@Repository
interface StudentGroupTokenRepository : EntityRepository<StudentGroupToken> {

    fun findByValue(value: String?): StudentGroupToken?
}


