package trik.testsys.webapp.backoffice.data.repository

import org.springframework.stereotype.Repository
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.entity.impl.Task
import trik.testsys.webapp.core.data.repository.EntityRepository

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Repository
interface SolutionRepository : EntityRepository<Solution> {

    fun findAllByTaskInAndContestNull(tasks: List<Task>): List<Solution>
}