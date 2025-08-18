//package trik.testsys.webapp.grading
//
//import com.google.protobuf.Empty
//import io.grpc.ManagedChannelBuilder
//import io.grpc.StatusException
//import kotlinx.coroutines.*
//import kotlinx.coroutines.flow.*
//import org.slf4j.LoggerFactory
//import org.springframework.stereotype.Service
//import trik.testsys.backoffice.entity.impl.Solution
//import trik.testsys.backoffice.service.FileManager
//import trik.testsys.backoffice.service.Grader
//import trik.testsys.backoffice.service.Grader.*
//import trik.testsys.grading.GradingNodeOuterClass.Submission
//import trik.testsys.grading.GradingNodeOuterClass.Result
//import trik.testsys.grading.converter.FieldResultConverter
//import trik.testsys.grading.converter.FileConverter
//import trik.testsys.grading.converter.ResultConverter
//import trik.testsys.grading.converter.SubmissionBuilder
//import trik.testsys.backoffice.service.entity.impl.SolutionService
//import java.time.Duration
//import java.time.LocalDateTime
//import java.util.concurrent.ConcurrentHashMap
//import java.util.concurrent.LinkedBlockingQueue
//
//@Service
//@Suppress("unused")
//class BalancingGraderService(
//    private val fileManager: FileManager,
//    private val solutionService: SolutionService
//): Grader {
//
//    private val replayCount = 1
//    // TODO: Use proper type for intervals and timeouts
//    private val statusRequestTimeout = 2000L
//    private val nodePollingInterval = 1000L
//    private val resendHangingSubmissionsInterval = 60000L
//    // TODO: get from status per each grading node
//    private val hangTimeout = 2 * 5 * 60 * 1000L
//    private val log = LoggerFactory.getLogger(this.javaClass)
//    private data class SubmissionInfo(
//        val solution: Solution,
//        val submission: Submission,
//        val sentTime: LocalDateTime,
//    )
//    private data class NodeInfo (
//        val node: GradingNodeGrpcKt.GradingNodeCoroutineStub,
//        val submissions: MutableSharedFlow<Submission>,
//        val results: Flow<Result>,
//        val sentSubmissions: LinkedBlockingQueue<SubmissionInfo> = LinkedBlockingQueue(),
//    )
//    private val nodes = ConcurrentHashMap<String, NodeInfo>()
//    private val submissionQueue = LinkedBlockingQueue<SubmissionInfo>()
//
//    private val subscriptions = mutableListOf<(GradingInfo) -> Unit>()
//    private val converter = ResultConverter(FieldResultConverter(FileConverter()))
//
//    private val resultProcessingScope = CoroutineScope(Dispatchers.Default)
//
//    private val submissionSendScope = CoroutineScope(Dispatchers.IO)
//    private val submissionSendJob = submissionSendScope.launch {
//        while (isActive) {
//            try {
//                val submissionInfo = submissionQueue.take()
//                var nodeInfo = findFreeNode()
//                while (nodeInfo == null) {
//                    delay(nodePollingInterval)
//                    nodeInfo = findFreeNode()
//                }
//                val submission = submissionInfo.submission
//                val solution = submissionInfo.solution
//                nodeInfo.sentSubmissions.add(submissionInfo)
//                nodeInfo.submissions.emit(submission)
//                log.info("Submission[id=${submission.id}] emitted")
//                resultProcessingScope.launch {
//                    processResults(nodeInfo.results, nodeInfo.sentSubmissions)
//                }
//
//                solution.status = Solution.SolutionStatus.IN_PROGRESS
//                solutionService.save(solution)
//            } catch (e: Exception) {
//                log.error("Submission send job iteration end up with error:", e)
//            }
//        }
//    }
//
//    private val resendHangingSubmissionsJob = submissionSendScope.launch {
//        while (isActive) {
//            try {
//                delay(resendHangingSubmissionsInterval)
//
//                val currentTime = LocalDateTime.now()
//
//                for ((ipPort, nodeInfo) in nodes) {
//                    for (submission in nodeInfo.sentSubmissions) {
//                        if (Duration.between(currentTime, submission.sentTime) > Duration.ofMillis(hangTimeout)) {
//                            log.warn("Resend hanging submission[${submission.submission.id}] on node[${ipPort}]")
//                            submissionQueue.add(submission)
//                            nodeInfo.sentSubmissions.remove(submission)
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                log.error("Submission resend job iteration end up with error:", e)
//            }
//        }
//    }
//
//    private fun resendSubmissions(sentSubmissions: LinkedBlockingQueue<SubmissionInfo>) {
//        if (sentSubmissions.isNotEmpty()) {
//            log.warn("${sentSubmissions.size} submissions are ungraded. Resending them")
//            sentSubmissions.drainTo(submissionQueue)
//        }
//    }
//
//    private suspend fun processResults(
//        results: Flow<Result>,
//        sentSubmissions: LinkedBlockingQueue<SubmissionInfo>
//    ) = withContext(Dispatchers.IO) {
//        try {
//            results.collect { result ->
//                val gradingInfo = converter.convert(result)
//                log.info("Got result for submission[id=${gradingInfo.submissionId}]")
//                sentSubmissions.removeIf { (_, submission) -> submission.id == gradingInfo.submissionId }
//                subscriptions.forEach { it.invoke(gradingInfo) }
//            }
//            log.debug("Finished processing results")
//        } catch (se: StatusException) {
//            val status = se.status
//            log.warn("RPC is finished with status code ${status.code.value()}, description ${status.description}, cause ${status.cause}")
//            resendSubmissions(sentSubmissions)
//        } catch (e: Exception) {
//            log.error("Unexpected error while processing results", e)
//            resendSubmissions(sentSubmissions)
//        }
//    }
//
//    private fun findFreeNode(): NodeInfo? =
//        getAllNodeStatuses()
//            .mapNotNull {
//                val aliveStatus = (it.value as? NodeStatus.Alive) ?: return@mapNotNull null
//                log.debug("Get status for node[id=${aliveStatus.id}, queued=${aliveStatus.queued}, capacity=${aliveStatus.capacity}]")
//                val nodeInfo = nodes[it.key]
//                if (nodeInfo != null && nodeInfo.sentSubmissions.size < aliveStatus.capacity)
//                    nodeInfo to aliveStatus
//                else null
//            }
//            .minByOrNull { (nodeInfo, status) ->
//                nodeInfo.sentSubmissions.size.toDouble() / status.capacity
//            }
//            ?.first
//
//    override fun sendToGrade(solution: Solution, gradingOptions: GradingOptions) {
//        val taskFiles = solution.task.polygons.mapNotNull { fileManager.getTaskFile(it) }
//        val solutionFile = fileManager.getSolutionFile(solution) ?: throw IllegalArgumentException("Cannot find solution file")
//
//        val submission = SubmissionBuilder.build {
//            this.solution = solution
//            this.solutionFile = solutionFile
//            this.task = solution.task
//            this.taskFiles = taskFiles
//            this.gradingOptions = gradingOptions
//        }
//
//        submissionQueue.put(SubmissionInfo(solution, submission, LocalDateTime.now()))
//    }
//
//    override fun subscribeOnGraded(onGraded: (GradingInfo) -> Unit) {
//        subscriptions.add(onGraded)
//    }
//
//    override suspend fun addNode(address: String) {
//        log.info("Adding node with address '$address'")
//
//        try {
//            val channel = ManagedChannelBuilder.forTarget(address)
//                .usePlaintext() // TODO: Make proper channel initialization
//                .maxInboundMessageSize(400_000_000)
//                .maxInboundMetadataSize(400_000_000)
//                .build()
//
//            val node = GradingNodeGrpcKt.GradingNodeCoroutineStub(channel)
//            // grpc backend becomes listener of submissions only after the solution is sent, thus need to replay submission for it
//            val submissions = MutableSharedFlow<Submission>(replayCount)
//            val results = node.grade(submissions)
//            nodes[address] = NodeInfo(node, submissions, results)
//
//            log.info("Added node with address '$address'")
//        } catch (se: StatusException) {
//            log.error("The grade request end up with status error (code ${se.status.code})")
//        } catch (e: Exception) {
//            log.error("The grade request end up with error:", e)
//        }
//    }
//
//    override fun removeNode(address: String) {
//        TODO("Need discussion. Docs say that node's channel should be closed by server with Status.OK code for proper termination")
//    }
//
//    private suspend fun getNodeStatus(nodeInfo: NodeInfo): NodeStatus = coroutineScope {
//        val node = nodeInfo.node
//        try {
//            withTimeout(statusRequestTimeout) {
//                val status = node.getStatus(Empty.getDefaultInstance())
//                NodeStatus.Alive(status.id, status.queued, status.capacity)
//            }
//        } catch (se: StatusException) {
//            NodeStatus.Unreachable("The request end up with status error (code ${se.status.code})")
//        } catch (tce: TimeoutCancellationException) {
//            NodeStatus.Unreachable("Status request timeout reached")
//        }
//    }
//
//    /**
//     * @param address address of the node
//     * @return [NodeStatus] instance or null if no node with given [address] is tracked
//     */
//    override fun getNodeStatus(address: String): NodeStatus? {
//        val nodeInfo = nodes[address] ?: return null
//        return runBlocking { getNodeStatus(nodeInfo) }
//    }
//
//    override fun getAllNodeStatuses(): Map<String, NodeStatus> {
//        val nodeStatuses = runBlocking {
//            nodes.mapValues { async { getNodeStatus(it.value) } }
//                .mapValues { it.value.await() }
//        }
//
//        nodeStatuses.forEach { (address, nodeStatus) ->
//            when (nodeStatus) {
//                is NodeStatus.Alive -> log.debug("Node with ID ${nodeStatus.id} available by address $address (${nodeStatus.queued}/${nodeStatus.capacity})")
//                is NodeStatus.Unreachable -> log.warn("Node is not available by address $address: ${nodeStatus.reason}")
//            }
//        }
//
//        return nodeStatuses
//    }
//}
