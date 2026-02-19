package trik.testsys.webapp.backoffice.data.service

import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.entity.impl.Verdict
import trik.testsys.webapp.core.data.service.EntityService

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
interface VerdictService : EntityService<Verdict> {

    fun createNewForSolution(solution: Solution, score: Long): Verdict

    fun findAllBySolutions(solutions: Collection<Solution>): Set<Verdict>

    fun findAllBySolutionIds(solutionIds: Collection<Long>): Set<Verdict>

    fun findAllForSolution(solutionId: Long): Set<Verdict>
}