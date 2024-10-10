package trik.testsys.webclient.view.impl

import trik.testsys.webclient.entity.impl.Contest
import trik.testsys.webclient.util.atTimeZone
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
    override val note: String
) : NotedEntityView<Contest> {

    override fun toEntity(timeZoneId: String?) = Contest(
        name
    ).also {
        it.id = id
        it.additionalInfo = additionalInfo
        it.note = note
    }

    companion object {

        fun Contest.toView(timeZone: String?) = ContestView(
            id = this.id,
            additionalInfo = this.additionalInfo,
            creationDate = this.creationDate?.atTimeZone(timeZone),
            name = this.name,
            note = this.note
        )
    }
}