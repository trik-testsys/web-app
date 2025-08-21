package trik.testsys.webapp.grading

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.entity.impl.Verdict
import trik.testsys.webapp.backoffice.data.service.VerdictService
import trik.testsys.webapp.backoffice.data.service.SolutionService
import trik.testsys.webapp.backoffice.service.Grader

@Service
@Primary
class StubGraderService(
    private val verdictService: VerdictService,
    private val solutionService: SolutionService,
) : Grader {

    private val log = LoggerFactory.getLogger(StubGraderService::class.java)
    private val subscribers = mutableListOf<(Grader.GradingInfo) -> Unit>()

    override fun sendToGrade(solution: Solution, gradingOptions: Grader.GradingOptions) {
        log.info("Stub grading started for solution id=${solution.id} with ${gradingOptions.trikStudioVersion}")

        // Synchronously assign a stub verdict and status for testing
        // Persist a verdict tied to this solution and update solution fields in the DB
        val verdict = Verdict().also {
            it.value = 100
            it.solution = solution
        }
        val persistedVerdict = verdictService.save(verdict)
        solution.relevantVerdict = persistedVerdict
        solution.status = Solution.Status.PASSED
        solutionService.save(solution)

        // Notify subscribers with a fake OK event (use a simple numeric id wrapper)
        subscribers.forEach { it.invoke(Grader.GradingInfo.Ok(solution.id?.toInt() ?: -1, emptyList())) }
    }

    override fun subscribeOnGraded(onGraded: (Grader.GradingInfo) -> Unit) {
        subscribers.add(onGraded)
    }

    override fun addNode(address: String) { /* no-op */ }

    override fun removeNode(address: String) { /* no-op */ }

    override fun getNodeStatus(address: String): Grader.NodeStatus? = null

    override fun getAllNodeStatuses(): Map<String, Grader.NodeStatus> = emptyMap()
}


