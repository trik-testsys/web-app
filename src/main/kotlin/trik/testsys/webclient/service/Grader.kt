package trik.testsys.webclient.service

import trik.testsys.webclient.entity.Solution
import trik.testsys.webclient.entity.Task
import java.nio.ByteBuffer

interface Grader {

    fun sendToGrade(solution: Solution, task: Task, gradingOptions: GradingOptions)

    fun subscribeOnGraded(onGraded: (GradingInfo) -> Unit)

    fun addNode(address: String)

    fun removeNode(address: String)

    fun getNodeStatus(address: String): NodeStatus?

    fun getAllNodeStatuses(): Map<String, NodeStatus>

    sealed class GradingInfo(open val submissionId: Int) {

        data class Error(override val submissionId: Int, val kind: Int, val description: String): GradingInfo(submissionId)

        data class File(val name: String, val content: ByteBuffer)

        data class FieldResult(val name: String, val verdict: File, val recording: File?)

        data class Ok(override val submissionId: Int, val fieldResults: List<FieldResult>): GradingInfo(submissionId)

    }

    sealed class NodeStatus {
        data class Alive(val id: Int, val capacity: Int, val queued: Int): NodeStatus()
        data class Unreachable(val reason: String): NodeStatus()
    }

    data class GradingOptions(
        val shouldRecordRun: Boolean,
        val trikStudioVersion: String,
    )
}
