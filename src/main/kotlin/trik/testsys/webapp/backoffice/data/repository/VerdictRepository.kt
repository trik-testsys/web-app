package trik.testsys.webapp.backoffice.data.repository

import org.springframework.stereotype.Repository
import trik.testsys.webapp.backoffice.data.entity.impl.Verdict
import trik.testsys.webapp.core.data.repository.EntityRepository

@Repository
interface VerdictRepository : EntityRepository<Verdict> {
}