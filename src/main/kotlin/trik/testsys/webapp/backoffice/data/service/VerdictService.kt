package trik.testsys.webapp.backoffice.data.service

import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.entity.impl.Verdict

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
interface VerdictService {

    fun save(verdict: Verdict): Verdict

    fun createNewForSolution(solution: Solution, score: Long): Verdict
}