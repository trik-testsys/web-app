package trik.testsys.webclient.view.impl

import org.springframework.format.annotation.DateTimeFormat
import trik.testsys.webclient.entity.impl.Contest
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.util.fromTimeZone
import trik.testsys.webclient.view.NotedEntityView
import java.sql.Time
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
data class ContestView(
    override val id: Long?,
    override val additionalInfo: String,
    override val creationDate: LocalDateTime?,
    override val name: String,
    override val note: String,
    val visibility: Contest.Visibility,
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    val startDate: LocalDateTime,
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    val endDate: LocalDateTime,
    val duration: LocalTime
) : NotedEntityView<Contest> {

    override fun toEntity(timeZoneId: String?) = Contest(
        name, startDate.fromTimeZone(timeZoneId), endDate.fromTimeZone(timeZoneId), duration
    ).also {
        it.id = id
        it.additionalInfo = additionalInfo
        it.note = note
    }

    val formattedStartDate: String
        get() = startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))

    val formattedEndDate: String
        get() = endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))

    companion object {

        fun Contest.toView(timeZone: String?) = ContestView(
            id = this.id,
            additionalInfo = this.additionalInfo,
            creationDate = this.creationDate?.atTimeZone(timeZone),
            name = this.name,
            note = this.note,
            visibility = this.visibility,
            startDate = this.startDate.atTimeZone(timeZone),
            endDate = this.endDate.atTimeZone(timeZone),
            duration = this.duration
        )
    }
}