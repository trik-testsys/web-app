package trik.testsys.grading

import com.google.protobuf.Empty
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import trik.testsys.webclient.entity.impl.Solution
import trik.testsys.webclient.service.FileManager
import trik.testsys.webclient.service.Grader
import trik.testsys.webclient.service.Grader.*
import trik.testsys.grading.GradingNodeOuterClass.Submission
import trik.testsys.grading.GradingNodeOuterClass.Result
import trik.testsys.grading.converter.FieldResultConverter
import trik.testsys.grading.converter.FileConverter
import trik.testsys.grading.converter.ResultConverter
import trik.testsys.grading.converter.SubmissionBuilder
import trik.testsys.webclient.service.entity.impl.SolutionService
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue

@Service
class BalancingGraderService(
    private val fileManager: FileManager,
    private val solutionService: SolutionService
): Grader {

    private val replayCount = 1
    private val statusRequestTimeout = 2000L
    private val nodePollingInterval = 1000L
    private val log = LoggerFactory.getLogger(this.javaClass)

    private data class NodeInfo (
        val node: GradingNodeGrpcKt.GradingNodeCoroutineStub,
        val submissions: MutableSharedFlow<Submission>,
        val results: Flow<Result>,
    )
    private val nodes = ConcurrentHashMap<String, NodeInfo>()
    private val submissionQueue = LinkedBlockingQueue<Pair<Solution, Submission>>()

    private val subscriptions = mutableListOf<(GradingInfo) -> Unit>()
    private val converter = ResultConverter(FieldResultConverter(FileConverter()))

    private val resultProcessingScope = CoroutineScope(Dispatchers.Default)

    private val submissionSendScope = CoroutineScope(Dispatchers.IO)
    private val submissionSendJob = submissionSendScope.launch {
        while (isActive) {
            val (solution, submission) = submissionQueue.take()
            var nodeInfo = findFreeNode()
            while (nodeInfo == null) {
                delay(nodePollingInterval)
                nodeInfo = findFreeNode()
            }

            nodeInfo.submissions.emit(submission)
            log.info("Submission[id=${submission.id}] emitted")
            resultProcessingScope.launch {
                processResults(nodeInfo.results)
            }

            solution.status = Solution.SolutionStatus.IN_PROGRESS
            solutionService.save(solution)
        }
    }

    private suspend fun processResults(results: Flow<Result>) = withContext(Dispatchers.IO) {
        try {
            results.collect { result ->
                val gradingInfo = converter.convert(result)
                log.info("Got result for submission[id=${gradingInfo.submissionId}]")
                subscriptions.forEach { it.invoke(gradingInfo) }
            }
            log.debug("Finished processing results")
        } catch (se: StatusException) {
            log.warn("RPC is finished with status code ${se.status.code.value()}", se)
        } catch (e: Exception) {
            log.error("Unexpected error while processing results", e)
        }
    }

    private fun findFreeNode(): NodeInfo? =
        getAllNodeStatuses()
            .mapNotNull {
                val aliveStatus = (it.value as? NodeStatus.Alive) ?: return@mapNotNull null
                log.debug("Get status for node[id=${aliveStatus.id}, queued=${aliveStatus.queued}, capacity=${aliveStatus.capacity}]")
                if (aliveStatus.queued < aliveStatus.capacity)
                    it.key to aliveStatus
                else null
            }
            .minByOrNull { (_, status) ->
                status.queued.toDouble() / status.capacity
            }
            ?.first
            ?.let(nodes::get)

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

        submissionQueue.put(solution to submission)
    }

    override fun subscribeOnGraded(onGraded: (GradingInfo) -> Unit) {
        subscriptions.add(onGraded)
    }

    override fun addNode(address: String) {
        val channel = ManagedChannelBuilder.forTarget(address)
            .usePlaintext() // TODO: Make proper channel initialization
            .build()

        val node = GradingNodeGrpcKt.GradingNodeCoroutineStub(channel)
        // grpc backend becomes listener of submissions only after the solution is sent, thus need to replay submission for it
        val submissions = MutableSharedFlow<Submission>(replayCount)
        val results = node.grade(submissions)
        nodes[address] = NodeInfo(node, submissions, results)
    }

    override fun removeNode(address: String) {
        TODO("Need discussion. Docs say that node's channel should be closed by server with Status.OK code for proper termination")
    }

    private suspend fun getNodeStatus(nodeInfo: NodeInfo): NodeStatus = coroutineScope {
        val node = nodeInfo.node
        try {
            withTimeout(statusRequestTimeout) {
                val status = node.getStatus(Empty.getDefaultInstance())
                NodeStatus.Alive(status.id, status.queued, status.capacity)
            }
        } catch (se: StatusException) {
            NodeStatus.Unreachable("The request end up with status error (code ${se.status.code})")
        } catch (tce: TimeoutCancellationException) {
            NodeStatus.Unreachable("Status request timeout reached")
        }
    }

    /**
     * @param address address of the node
     * @return [NodeStatus] instance or null if no node with given [address] is tracked
     */
    override fun getNodeStatus(address: String): NodeStatus? {
        val nodeInfo = nodes[address] ?: return null
        return runBlocking { getNodeStatus(nodeInfo) }
    }

    override fun getAllNodeStatuses(): Map<String, NodeStatus> {
        return runBlocking {
            nodes.mapValues { async { getNodeStatus(it.value) } }
                .mapValues { it.value.await() }
        }
    }
}
