package trik.testsys.webapp.grading

import io.grpc.StatusException
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import trik.testsys.grading.GradingNodeOuterClass.Submission
import trik.testsys.webapp.grading.communication.GradingNodeManager
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.service.SolutionService
import trik.testsys.webapp.backoffice.service.FileManager
import trik.testsys.webapp.backoffice.service.Grader
import trik.testsys.webapp.grading.converter.SubmissionBuilder
import java.time.LocalDateTime

data class SubmissionInfo(
    val solution: Solution,
    val submission: Submission,
    val sentTime: LocalDateTime,
)

@Primary
@Service
class BalancingGraderService(
    private val fileManager: FileManager,
    private val solutionService: SolutionService,
    configuration: GraderConfiguration,
): Grader {

    private val nodeManager = GradingNodeManager(configuration)
    private val gradingManager = GradingManager(
        nodeManager,
        configuration,
        onSent = { submission ->
            val solution = submission.solution
            solution.status = Solution.Status.IN_PROGRESS
            solutionService.save(solution)
        }
    )
    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun sendToGrade(solution: Solution, gradingOptions: Grader.GradingOptions) {
        val taskFiles = solution.task.polygons.mapNotNull { fileManager.getTaskFile(it) }
        val solutionFile = fileManager.getSolutionFile(solution) ?: throw IllegalArgumentException("Cannot find solution file")

        val submission = SubmissionBuilder.build {
            this.solution = solution
            this.solutionFile = solutionFile
            this.task = solution.task
            this.taskFiles = taskFiles
            this.gradingOptions = gradingOptions
        }

        gradingManager.enqueueSubmission(SubmissionInfo(solution, submission, LocalDateTime.now()))
    }

    override fun subscribeOnGraded(onGraded: (Grader.GradingInfo) -> Unit) {
        nodeManager.subscribeOnGraded(onGraded)
    }

    override fun addNode(address: String) {
        log.info("Adding node with address '$address'")

        try {
            nodeManager.addNode(address)
            log.info("Added node with address '$address'")
        } catch (se: StatusException) {
            log.error("The grade request end up with status error (code ${se.status.code})")
        } catch (e: Exception) {
            log.error("The grade request end up with error:", e)
        }
    }

    override fun removeNode(address: String) {
        // message below does not correspond to the reality
        // because of changed type of communication from streaming to direct call
        TODO("Need discussion. Docs say that node's channel should be closed by server with Status.OK code for proper termination")
    }

    /**
     * @param address address of the node
     * @return [NodeStatus] instance or null if no node with given [address] is tracked
     */
    override fun getNodeStatus(address: String): Grader.NodeStatus? {
        return nodeManager.getNodeStatus(address)
    }

    override fun getAllNodeStatuses(): Map<String, Grader.NodeStatus> {
        return nodeManager.getAllNodeStatuses()
    }
}
