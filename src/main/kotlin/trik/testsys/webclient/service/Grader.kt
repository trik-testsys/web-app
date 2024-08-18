package trik.testsys.webclient.service

import trik.testsys.webclient.entity.Solution
import trik.testsys.webclient.entity.Task

interface Grader {

    fun sendToGrade(solution: Solution, task: Task, gradingOptions: GradingOptions)

    fun subscribeOnGraded(onGraded: (GradingInfo) -> Unit)

    fun addNode(address: String)

    fun removeNode(address: String)

    fun getNodeStatus(address: String): NodeStatus

    fun getAllNodeStatuses(): Map<String, NodeStatus>

    data class GradingInfo(
        val solutionId: Long,
    )

    data class NodeStatus(
        val isAlive: Boolean,
    )

    data class GradingOptions(
        val shouldRecordRun: Boolean,
        val trikStudioVersion: String,
    )
}