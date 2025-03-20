package trik.testsys.webclient.view.impl

import trik.testsys.core.view.named.NamedEntityView
import trik.testsys.webclient.entity.impl.Contest
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.util.format
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class StudentContestView(
    override val id: Long?,
    override val additionalInfo: String,
    override val creationDate: LocalDateTime?,
    override val name: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val lastTime: String,
    val isGoingOn: Boolean,
    val duration: LocalTime,
    val tasks: List<TaskView> = emptyList(),
    val outdated: Boolean = false
) : NamedEntityView<Contest> {

    override fun toEntity(timeZoneId: String?) = TODO()

    val formattedStartDate: String
        get() = startDate.format()

    val formattedEndDate: String
        get() = endDate.format()

    companion object {

        fun Contest.toStudentView(timeZone: String?, outdated: Boolean = true) = StudentContestView(
            id = this.id,
            additionalInfo = this.additionalInfo,
            creationDate = this.creationDate?.atTimeZone(timeZone),
            name = this.name,
            startDate = this.startDate.atTimeZone(timeZone),
            endDate = this.endDate.atTimeZone(timeZone),
            isGoingOn = this.isGoingOn(),
            duration = this.duration,
            lastTime = "—",
            outdated = outdated
        )

        fun Contest.toStudentView(timeZone: String?, lastTime: LocalTime) = StudentContestView(
            id = this.id,
            additionalInfo = this.additionalInfo,
            creationDate = this.creationDate?.atTimeZone(timeZone),
            name = this.name,
            lastTime = if (isOpenEnded) "Неограниченно" else lastTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            startDate = this.startDate.atTimeZone(timeZone),
            endDate = this.endDate.atTimeZone(timeZone),
            isGoingOn = this.isGoingOn(),
            duration = this.duration
        )
    }
}
