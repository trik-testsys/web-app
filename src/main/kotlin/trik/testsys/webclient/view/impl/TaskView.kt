package trik.testsys.webclient.view.impl

import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.view.NotedEntityView
import java.time.LocalDateTime
import java.util.*

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
data class TaskView(
    override val id: Long?,
    override val name: String,
    override val creationDate: LocalDateTime?,
    override val additionalInfo: String,
    override val note: String
) : NotedEntityView<Task> {

    override fun toEntity(timeZone: TimeZone) = Task(
        name
    ).also {
        it.id = id
        it.additionalInfo = additionalInfo
        it.note = note
    }

    companion object {

        fun Task.toView(timeZone: TimeZone) = TaskView(
            id = id,
            name = name,
            creationDate = creationDate?.atTimeZone(timeZone),
            additionalInfo = additionalInfo,
            note = note
        )
    }
}