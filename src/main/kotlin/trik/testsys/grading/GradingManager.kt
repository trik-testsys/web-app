package trik.testsys.grading

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import trik.testsys.grading.communication.GradingNodeManager
import trik.testsys.webclient.entity.impl.Solution
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.LinkedBlockingQueue

@Suppress("UNUSED")
class GradingManager(
    private val nodeManager: GradingNodeManager,
    private val configuration: GraderConfiguration,
    private val onSent: (SubmissionInfo) -> Unit,
) {
    private val submissionQueue = LinkedBlockingQueue<SubmissionInfo>()

    init {
        nodeManager.subscribeOnNodeFail {
            it.drainSubmissionsTo(submissionQueue)
        }
    }

    private val log = LoggerFactory.getLogger(this.javaClass)
    private val submissionSendScope = CoroutineScope(Dispatchers.IO)
    private val submissionSendJob = submissionSendScope.launch {
        while (isActive) {
            try {
                val submissionInfo = submissionQueue.take()
                var address = findFreeNode()
                while (address == null) {
                    delay(configuration.nodePollingInterval.inWholeMilliseconds)
                    address = findFreeNode()
                }

                nodeManager.grade(address, submissionInfo)
                onSent(submissionInfo)
            } catch (e: Exception) {
                log.sendCycleError(e)
            }
        }
    }

    private fun findFreeNode(): String? {
        val statuses = nodeManager.getClients2Statuses()
        return BalancingUtils.findOptimalNode(statuses)
    }

    private val resendHangingSubmissionsJob = submissionSendScope.launch {
        while (isActive) {
            try {
                delay(configuration.resendHangingSubmissionsInterval.inWholeMilliseconds)

                for ((_, client) in nodeManager.nodes) {
                    client.drainHangingSubmissionsTo(submissionQueue)
                }
            } catch (e: Exception) {
                log.resendHangingCycleError(e)
            }
        }
    }

    fun enqueueSubmission(submissionInfo: SubmissionInfo) {
        submissionQueue.put(submissionInfo)
    }

    companion object {
        private fun Logger.sendCycleError(e: Exception) {
            error("Submission send job iteration end up with error:", e)
        }

        private fun Logger.resendHangingCycleError(e: Exception) {
            error("Submission resend job iteration end up with error:", e)
        }
    }

}