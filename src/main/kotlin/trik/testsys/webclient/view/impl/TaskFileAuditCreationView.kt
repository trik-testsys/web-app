package trik.testsys.webclient.view.impl

import trik.testsys.webclient.entity.impl.TaskFile
import trik.testsys.webclient.entity.impl.TaskFileAudit

/**
 * @author Roman Shishkin
 * @since 2.1.0
 */
data class TaskFileAuditCreationView(
    val additionalInfo: String
) {

    fun toEntity(taskFile: TaskFile) = TaskFileAudit(
        taskFile
    ).also {
        it.additionalInfo = additionalInfo
    }

    companion object {

        fun empty() = TaskFileAuditCreationView("")
    }
}