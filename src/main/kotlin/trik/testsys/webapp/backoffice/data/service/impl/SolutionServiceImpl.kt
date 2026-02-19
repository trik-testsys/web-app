package trik.testsys.webapp.backoffice.data.service.impl

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.entity.impl.Task
import trik.testsys.webapp.backoffice.data.repository.SolutionRepository
import trik.testsys.webapp.backoffice.data.repository.support.SolutionSpecifications
import trik.testsys.webapp.backoffice.data.service.SolutionService
import trik.testsys.webapp.core.data.service.AbstractService
import java.time.Instant

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

    override fun findStudentSolutionsPage(
        studentId: Long?,
        groupId: Long?,
        adminId: Long?,
        viewerId: Long?,
        fromDate: Instant?,
        toDate: Instant?,
        pageable: Pageable,
    ): Page<Solution> {
        var spec = SolutionSpecifications.hasStudentPrivilege()
        if (studentId != null) spec = spec.and(SolutionSpecifications.createdBy(studentId))
        if (groupId != null) spec = spec.and(SolutionSpecifications.inGroup(groupId))
        if (adminId != null) spec = spec.and(SolutionSpecifications.underAdmin(adminId))
        if (viewerId != null) spec = spec.and(SolutionSpecifications.underViewer(viewerId))
        if (fromDate != null) spec = spec.and(SolutionSpecifications.createdAfter(fromDate))
        if (toDate != null) spec = spec.and(SolutionSpecifications.createdBefore(toDate))
        return findAll(spec, pageable)
    }

    companion object {

        private val logger = LoggerFactory.getLogger(SolutionServiceImpl::class.java)
    }
}
