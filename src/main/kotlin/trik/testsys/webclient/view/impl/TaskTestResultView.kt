package trik.testsys.webclient.view.impl

import trik.testsys.webclient.entity.impl.Solution
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.util.format

data class TaskTestResultView(
    val id: Long?,
    val taskName: String,
    val creationDate: String?,
    val status: Solution.SolutionStatus,
    val score: Long,
    val additionalInfo: String
) {

    companion object {

        fun Solution.toTaskTestResultView(timeZoneId: String?) = TaskTestResultView(
            id = id,
            creationDate = creationDate?.atTimeZone(timeZoneId)?.format(),
            status = status,
            score = score,
            taskName = "${task.id}: ${task.name}",
            additionalInfo = additionalInfo
        )
    }
}