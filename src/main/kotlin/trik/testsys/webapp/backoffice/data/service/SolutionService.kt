package trik.testsys.webapp.backoffice.data.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.entity.impl.Task
import trik.testsys.webapp.core.data.service.EntityService
import java.time.Instant

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
interface SolutionService : EntityService<Solution> {

    fun findAllTestingSolutions(task: Task): List<Solution> = findAllTestingSolutions(listOf(task))

    fun findAllTestingSolutions(tasks: List<Task>): List<Solution>

    fun updateStatus(solutionId: Long, newStatus: Solution.Status)

    fun findStudentSolutionsPage(
        studentId: Long?,
        groupId: Long?,
        adminId: Long?,
        viewerId: Long?,
        fromDate: Instant?,
        toDate: Instant?,
        pageable: Pageable,
    ): Page<Solution>
}