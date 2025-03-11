package trik.testsys.webclient.view.impl

import org.springframework.format.annotation.DateTimeFormat
import trik.testsys.webclient.entity.impl.Contest
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.util.convertToLocalTime
import trik.testsys.webclient.util.format
import trik.testsys.webclient.util.fromTimeZone
import trik.testsys.webclient.view.NotedEntityView
import java.time.LocalDateTime

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
    val duration: String
) : NotedEntityView<Contest> {

    override fun toEntity(timeZoneId: String?) = Contest(
        name, startDate.fromTimeZone(timeZoneId), endDate.fromTimeZone(timeZoneId), duration.convertToLocalTime()
    ).also {
        it.id = id
        it.additionalInfo = additionalInfo
        it.note = note
        it.visibility = visibility
    }

    val formattedStartDate: String
        get() = startDate.format()

    val formattedEndDate: String
        get() = endDate.format()

    val formattedDuration: String
        get() = duration.format()

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
            duration = this.duration.toString()
        )
    }
}