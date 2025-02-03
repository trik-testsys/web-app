package trik.testsys.webclient.view.impl

import trik.testsys.webclient.entity.impl.TaskFileAudit
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.util.format

/**
 * @author Roman Shishkin
 * @since 2.1.0
 */
data class TaskFileAuditView(
    val id: Long?,
    val additionalInfo: String,
    val creationDate: String?
) {

    companion object {

        fun TaskFileAudit.toView(timeZone: String?) = TaskFileAuditView(
            id = id,
            additionalInfo = additionalInfo,
            creationDate = creationDate?.atTimeZone(timeZone)?.format()
        )
    }
}