package trik.testsys.webclient.view.impl

import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.view.NotedEntityView
import trik.testsys.webclient.view.impl.TaskFileView.Companion.toView
import java.time.LocalDateTime

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
data class TaskView(
    override val id: Long?,
    override val name: String,
    override val creationDate: LocalDateTime?,
    override val additionalInfo: String,
    override val note: String,
    val passedTest: Boolean = false,
    val taskFiles: List<TaskFileView> = emptyList(),
    val hasCondition: Boolean?
) : NotedEntityView<Task> {

    override fun toEntity(timeZoneId: String?) = Task(
        name
    ).also {
        it.id = id
        it.additionalInfo = additionalInfo
        it.note = note
    }

    companion object {

        fun Task.toView(timeZone: String?) = TaskView(
            id = id,
            name = name,
            creationDate = creationDate?.atTimeZone(timeZone),
            additionalInfo = additionalInfo,
            note = note,
            taskFiles = taskFiles.map { it.toView(timeZone) }.sortedBy { it.id },
            passedTest = passedTests,
            hasCondition = hasCondition
        )
    }
}