package trik.testsys.grading

import io.grpc.StatusException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import trik.testsys.webclient.entity.impl.Solution
import trik.testsys.webclient.service.FileManager
import trik.testsys.webclient.service.Grader
import trik.testsys.webclient.service.Grader.*
import trik.testsys.grading.GradingNodeOuterClass.Submission
import trik.testsys.grading.communication.GradingNodeManager
import trik.testsys.grading.converter.SubmissionBuilder
import trik.testsys.webclient.service.entity.impl.SolutionService
import java.time.LocalDateTime

data class SubmissionInfo(
    val solution: Solution,
    val submission: Submission,
    val sentTime: LocalDateTime,
)

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
            solution.status = Solution.SolutionStatus.IN_PROGRESS
            solutionService.save(solution)
        }
    )
    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun sendToGrade(solution: Solution, gradingOptions: GradingOptions) {
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

    override fun subscribeOnGraded(onGraded: (GradingInfo) -> Unit) {
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
    override fun getNodeStatus(address: String): NodeStatus? {
        return nodeManager.getNodeStatus(address)
    }

    override fun getAllNodeStatuses(): Map<String, NodeStatus> {
        return nodeManager.getAllNodeStatuses()
    }
}
