package trik.testsys.webclient.view.impl

import trik.testsys.webclient.entity.impl.TaskFile
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.view.NotedEntityView
import java.time.LocalDateTime
import java.util.TimeZone

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
data class TaskFileView(
    override val id: Long?,
    override val name: String,
    override val creationDate: LocalDateTime?,
    override val additionalInfo: String,
    override val note: String,
    val type: TaskFile.TaskFileType
) : NotedEntityView<TaskFile> {

    override fun toEntity(timeZone: TimeZone) = TaskFile(
        name, type
    ).also {
        it.id = id
        it.additionalInfo = additionalInfo
        it.note = note
    }

    companion object {

        fun TaskFile.toView(timeZone: TimeZone) = TaskFileView(
            id = id,
            name = name,
            creationDate = creationDate?.atTimeZone(timeZone),
            additionalInfo = additionalInfo,
            note = note,
            type = type
        )
    }
}