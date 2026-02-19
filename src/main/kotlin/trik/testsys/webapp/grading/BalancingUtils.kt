package trik.testsys.webapp.grading

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import trik.testsys.webapp.grading.communication.GraderClient
import trik.testsys.webapp.backoffice.service.Grader

/**
 * @author Vyacheslav Buchin
 * @since 3.12.0
 */
object BalancingUtils {
    private val log = LoggerFactory.getLogger(BalancingUtils::class.java)

    fun findOptimalNode(nodes: Map<GraderClient, Grader.NodeStatus>) =
        nodes.mapNotNull { (client, status) ->
            val aliveStatus = (status as? Grader.NodeStatus.Alive) ?: return@mapNotNull null
            log.gotAliveStatus(aliveStatus.id, aliveStatus.queued, aliveStatus.capacity)
            if (client.sentSubmissionsCount < aliveStatus.capacity)
                client to aliveStatus
            else null
        }.minByOrNull { (client, status) ->
            client.sentSubmissionsCount.toDouble() / status.capacity
        }
        ?.first?.address

    private fun Logger.gotAliveStatus(nodeId: Int, queued: Int, capacity: Int) {
        debug("Get status for node[id=${nodeId}, queued=${queued}, capacity=${capacity}]")
    }
}