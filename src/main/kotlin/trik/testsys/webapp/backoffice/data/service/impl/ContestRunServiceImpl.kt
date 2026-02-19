package trik.testsys.webapp.backoffice.data.service.impl

import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.Contest
import trik.testsys.webapp.backoffice.data.entity.impl.ContestRun
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.repository.ContestRunRepository
import trik.testsys.webapp.backoffice.data.service.ContestRunService
import trik.testsys.webapp.core.data.service.AbstractService
import java.time.Instant

@Service
class ContestRunServiceImpl :
    AbstractService<ContestRun, ContestRunRepository>(),
    ContestRunService {

    override fun findByUserAndContest(user: User, contest: Contest): ContestRun? =
        repository.findByUserAndContest(user, contest)

    override fun findAllByUser(user: User): Set<ContestRun> = repository.findAllByUser(user)

    override fun startIfAbsent(user: User, contest: Contest): ContestRun {
        val existing = repository.findByUserAndContest(user, contest)
        if (existing != null) return existing
        val run = ContestRun()
        run.user = user
        run.contest = contest
        run.startedAt = Instant.now()
        return save(run)
    }
}


