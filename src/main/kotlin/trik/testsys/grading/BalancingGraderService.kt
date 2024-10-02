package trik.testsys.grading

import com.google.protobuf.Empty
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Service
import trik.testsys.webclient.entity.impl.Solution
import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.service.FileManager
import trik.testsys.webclient.service.Grader
import trik.testsys.webclient.service.Grader.*
import trik.testsys.grading.GradingNodeOuterClass.Submission
import trik.testsys.grading.GradingNodeOuterClass.Result
import trik.testsys.grading.converter.FieldResultConverter
import trik.testsys.grading.converter.FileConverter
import trik.testsys.grading.converter.ResultConverter
import trik.testsys.grading.converter.SubmissionBuilder

@Service
class BalancingGraderService(private val fileManager: FileManager): Grader {

    private val statusRequestTimeout = 500L

    private data class NodeInfo (
        val node: GradingNodeGrpcKt.GradingNodeCoroutineStub,
        val submissions: MutableSharedFlow<Submission>,
        val results: Flow<Result>,
    )

    private val nodes = mutableMapOf<String, NodeInfo>()
    private val subscriptions = mutableListOf<(GradingInfo) -> Unit>()
    private val resultProcessingScope = CoroutineScope(Dispatchers.Default)
    private val converter = ResultConverter(FieldResultConverter(FileConverter()))

    private suspend fun processResults(results: Flow<Result>) {
        results.onEach { result ->
            val gradingInfo = converter.convert(result)
            subscriptions.forEach { it.invoke(gradingInfo) }
        }.collect()
    }

    private fun findOptimalNode(): NodeInfo {
        val optimalAddress = getAllNodeStatuses()
            .mapNotNull {
                val aliveStatus = (it.value as? NodeStatus.Alive) ?: return@mapNotNull null
                it.key to aliveStatus
            }
            .minByOrNull { (_, status) ->
                status.queued.toDouble() / status.capacity
            }
            ?.first
            ?: throw IllegalStateException("No available node to send submission")
        return nodes.getValue(optimalAddress)
    }

    override fun sendToGrade(solution: Solution, task: Task, gradingOptions: GradingOptions) {
        val taskFiles = task.polygons.mapNotNull { fileManager.getTaskFile(it) }
        val solutionFile = fileManager.getSolutionFile(solution) ?: throw IllegalArgumentException("Cannot find solution file")

        val submission = SubmissionBuilder.build {
            this.solution = solution
            this.solutionFile = solutionFile
            this.task = task
            this.taskFiles = taskFiles
            this.gradingOptions = gradingOptions
        }
        val node = findOptimalNode()

        resultProcessingScope.launch {
            node.submissions.emit(submission)
            processResults(node.results)
        }
    }

    override fun subscribeOnGraded(onGraded: (GradingInfo) -> Unit) {
        subscriptions.add(onGraded)
    }

    override fun addNode(address: String) {
        val channel = ManagedChannelBuilder.forTarget(address)
            .usePlaintext() // TODO: Make proper channel initialization
            .build()
        val node = GradingNodeGrpcKt.GradingNodeCoroutineStub(channel)
        val submissions = MutableSharedFlow<Submission>()
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
            NodeStatus.Unreachable("Request timeout reached")
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
