package trik.testsys.webapp.backoffice.data.repository

import org.springframework.stereotype.Repository
import trik.testsys.webapp.backoffice.data.entity.impl.Contest
import trik.testsys.webapp.backoffice.data.entity.impl.ContestRun
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.core.data.repository.EntityRepository

@Repository
interface ContestRunRepository : EntityRepository<ContestRun> {

    fun findByUserAndContest(user: User, contest: Contest): ContestRun?

    fun findAllByUser(user: User): Set<ContestRun>
}


