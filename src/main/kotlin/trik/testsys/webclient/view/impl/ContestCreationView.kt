package trik.testsys.webclient.view.impl

import org.springframework.format.annotation.DateTimeFormat
import trik.testsys.webclient.entity.impl.Contest
import trik.testsys.webclient.entity.user.impl.Developer
import trik.testsys.webclient.util.convertToLocalTime
import trik.testsys.webclient.util.fromTimeZone
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
data class ContestCreationView(
    val name: String,
    val additionalInfo: String,
    val note: String,
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    val startDate: LocalDateTime,
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    val endDate: LocalDateTime,
    val duration: String
) {

    fun toEntity(developer: Developer, timeZoneId: String?) = Contest(
        name,
        startDate.fromTimeZone(timeZoneId), endDate.fromTimeZone(timeZoneId),
        duration.convertToLocalTime()
    ).also {
        it.additionalInfo = additionalInfo
        it.note = note
        it.developer = developer
    }

    val formattedStartDate: String
        get() = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))

    val formattedEndDate: String
        get() = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))

    companion object {

        fun empty() = ContestCreationView(
            "", "", "",
            LocalDateTime.now(), LocalDateTime.now(), "01:00"
        )
    }
}