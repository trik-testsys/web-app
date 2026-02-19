package trik.testsys.webapp.grading.communication

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import trik.testsys.webapp.backoffice.service.Grader
import trik.testsys.webapp.grading.GraderConfiguration
import trik.testsys.webapp.grading.SubmissionInfo
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.mapKeys

/**
 * @author Vyacheslav Buchin
 * @since 3.12.0
 */
class GradingNodeManager(
    private val configuration: GraderConfiguration,
) {
    private val _nodes = ConcurrentHashMap<String, GraderClient>()
    private val onGradedSubscribers = mutableListOf<(Grader.GradingInfo) -> Unit>()
    private val onNodeFailSubscribers = mutableListOf<(GraderClient) -> Unit>()

    private val log = LoggerFactory.getLogger(this.javaClass)

    val nodes get() = _nodes.toMap()

    fun addNode(address: String) {
        _nodes[address] = GraderClient(address, configuration, this)
    }

    fun subscribeOnGraded(action: (Grader.GradingInfo) -> Unit) {
        onGradedSubscribers.add(action)
    }

    fun subscribeOnNodeFail(action: (GraderClient) -> Unit) {
        onNodeFailSubscribers.add(action)
    }

    fun handleGraded(result: Grader.GradingInfo) {
        onGradedSubscribers.forEach { it(result) }
    }

    fun handleCommunicationError(graderClient: GraderClient) {
        onNodeFailSubscribers.forEach { it(graderClient) }
    }

    fun grade(address: String, submission: SubmissionInfo) {
        val node = _nodes[address] ?: return
        node.grade(submission)
    }

    fun getNodeStatus(address: String): Grader.NodeStatus? {
        val node = _nodes[address] ?: return null
        return runBlocking { node.getStatus() }
    }

    fun getAllNodeStatuses(): Map<String, Grader.NodeStatus> {
        val nodeStatuses = runBlocking {
            _nodes.mapValues { async { it.value.getStatus() } }
                .mapValues { it.value.await() }
        }

        nodeStatuses.forEach { (address, nodeStatus) ->
            when (nodeStatus) {
                is Grader.NodeStatus.Alive -> log.nodeAlive(nodeStatus, address)
                is Grader.NodeStatus.Unreachable -> log.nodeUnreachable(nodeStatus, address)
            }
        }

        return nodeStatuses
    }

    fun getClients2Statuses(): Map<GraderClient, Grader.NodeStatus> {
        return getAllNodeStatuses().mapKeys { (address, _) -> _nodes.getValue(address) }
    }

    companion object {
        private fun Logger.nodeAlive(nodeStatus: Grader.NodeStatus.Alive, address: String) {
            debug("Node with ID ${nodeStatus.id} available by address $address (${nodeStatus.queued}/${nodeStatus.capacity})")
        }

        private fun Logger.nodeUnreachable(nodeStatus: Grader.NodeStatus.Unreachable, address: String) {
            warn("Node is not available by address $address: ${nodeStatus.reason}")
        }
    }
}