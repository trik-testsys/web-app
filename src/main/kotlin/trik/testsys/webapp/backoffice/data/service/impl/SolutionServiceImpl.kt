package trik.testsys.webapp.backoffice.data.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.entity.impl.Task
import trik.testsys.webapp.backoffice.data.repository.SolutionRepository
import trik.testsys.webapp.backoffice.data.service.SolutionService
import trik.testsys.webapp.core.data.service.AbstractService

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
class SolutionServiceImpl :
    AbstractService<Solution, SolutionRepository>(),
    SolutionService {

    override fun findAllTestingSolutions(tasks: List<Task>): List<Solution> {
        val allTesting = repository.findAllByTaskInAndContestNull(tasks)
        return allTesting
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun updateStatus(solutionId: Long, newStatus: Solution.Status) {
        logger.debug("Called SolutionService.updateStatus(solutionId=${solutionId}, newStatus=${newStatus})")
        findById(solutionId)?.let {
            it.status = newStatus
            save(it)
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(SolutionServiceImpl::class.java)
    }
}
