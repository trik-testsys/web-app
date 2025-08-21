package trik.testsys.webapp.grading.communication

import com.google.protobuf.Empty
import io.grpc.Channel
import io.grpc.ManagedChannelBuilder
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import trik.testsys.webapp.grading.GraderConfiguration
import trik.testsys.grading.GradingNodeGrpc
import trik.testsys.grading.GradingNodeOuterClass
import trik.testsys.webapp.backoffice.service.Grader
import trik.testsys.webapp.grading.SubmissionInfo
import trik.testsys.webapp.grading.converter.FieldResultConverter
import trik.testsys.webapp.grading.converter.FileConverter
import trik.testsys.webapp.grading.converter.ResultConverter
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.LinkedBlockingQueue

class GraderClient(
    val address: String,
    private val configuration: GraderConfiguration,
    private val nodeManager: GradingNodeManager,
) {
    private val gradingStub: GradingNodeGrpc.GradingNodeStub
    private val statusStub: GradingNodeGrpc.GradingNodeBlockingStub

    private val sentSubmissions = LinkedBlockingQueue<SubmissionInfo>()

    private val log = LoggerFactory.getLogger(this.javaClass)
    private val converter = ResultConverter(FieldResultConverter(FileConverter()))

    init {
        val channel: Channel = ManagedChannelBuilder.forTarget(address)
            .usePlaintext() // TODO: Make proper channel initialization
            .maxInboundMessageSize(MAX_MESSAGE_SIZE)
            .maxInboundMetadataSize(MAX_METADATA_SIZE)
            .build()
        gradingStub = GradingNodeGrpc.newStub(channel)
        statusStub = GradingNodeGrpc.newBlockingStub(channel)
    }

    fun grade(submission: SubmissionInfo) {
        gradingStub.grade(
            submission.submission,
            GradingResultObserver(submission),
        )
        sentSubmissions.add(submission)
        log.sentToGrade(submission, address)
    }

    suspend fun getStatus(): Grader.NodeStatus = coroutineScope {
        try {
            withTimeout(configuration.statusResponseTimeout.inWholeMilliseconds) {
                val status = statusStub.getStatus(Empty.getDefaultInstance())
                Grader.NodeStatus.Alive(status.id, status.queued, status.capacity)
            }
        } catch (se: StatusException) {
            Grader.NodeStatus.Unreachable("The request end up with status error (code ${se.status.code})")
        } catch (se: StatusRuntimeException) {
            Grader.NodeStatus.Unreachable("The request end up with status error (code ${se.status.code})")
        } catch (_: TimeoutCancellationException) {
            Grader.NodeStatus.Unreachable("Status request timeout reached")
        } catch (_: Exception) {
            Grader.NodeStatus.Unreachable("Unknown reason")
        }
    }

    val sentSubmissionsCount: Int get() = sentSubmissions.size

    fun drainHangingSubmissionsTo(to: MutableCollection<SubmissionInfo>) {
        val currentTime = LocalDateTime.now()
        val hangingSubmissions = mutableListOf<SubmissionInfo>()
        sentSubmissions.filterTo(hangingSubmissions) {
            Duration.between(currentTime, it.sentTime) > Duration.ofMillis(configuration.hangTimeout.inWholeMilliseconds)
        }
        to += hangingSubmissions
        sentSubmissions.removeAll(hangingSubmissions)
    }

    fun drainSubmissionsTo(to: MutableCollection<SubmissionInfo>) {
        sentSubmissions.drainTo(to)
    }

    private inner class GradingResultObserver(
        private val submissionInfo: SubmissionInfo,
    ) : StreamObserver<GradingNodeOuterClass.Result> {

        override fun onNext(value: GradingNodeOuterClass.Result?) {
            if (value == null) {
                log.nullGradingResult(
                    submissionId = submissionInfo.submission.id,
                    nodeAddress = address,
                )
                return
            }

            sentSubmissions.remove(submissionInfo)

            value
                .let(converter::convert)
                .also { log.graded(it) }
                .let(nodeManager::handleGraded)
        }

        override fun onError(t: Throwable?) {
            when (t) {
                is StatusException -> {
                    log.statusError(t.status)
                    nodeManager.handleCommunicationError(this@GraderClient)
                }
                is Exception -> {
                    log.unknownError(t)
                    nodeManager.handleCommunicationError(this@GraderClient)
                }
            }
        }

        override fun onCompleted() { }
    }

    companion object {
        private const val MAX_MESSAGE_SIZE = 400_000_000
        private const val MAX_METADATA_SIZE = 400_000_000

        private fun Logger.nullGradingResult(submissionId: Int, nodeAddress: String) {
            error("No grading result on submission[id=${submissionId}] from node[addr=${nodeAddress}]")
        }

        private fun Logger.statusError(status: Status) {
            error("RPC is finished with status code ${status.code.value()}, description ${status.description}, cause ${status.cause}")
        }

        private fun Logger.unknownError(e: Exception) {
            error("Unexpected error while processing results", e)
        }

        private fun Logger.graded(result: Grader.GradingInfo) {
            info("Submission[id=${result.submissionId}] graded")
        }

        private fun Logger.sentToGrade(submission: SubmissionInfo, nodeAddress: String) {
            debug("Submission[id=${submission.submission.id}] sent to grade on node[addr=${nodeAddress}]")
        }
    }
}