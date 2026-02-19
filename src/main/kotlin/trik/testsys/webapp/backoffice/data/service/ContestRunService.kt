package trik.testsys.webapp.backoffice.data.service

import trik.testsys.webapp.backoffice.data.entity.impl.Contest
import trik.testsys.webapp.backoffice.data.entity.impl.ContestRun
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.core.data.service.EntityService

interface ContestRunService : EntityService<ContestRun> {
    fun findByUserAndContest(user: User, contest: Contest): ContestRun?
    fun findAllByUser(user: User): Set<ContestRun>
    fun startIfAbsent(user: User, contest: Contest): ContestRun
}


