package trik.testsys.webclient.view.impl

import trik.testsys.webclient.entity.impl.SolutionVerdict
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.util.format
import trik.testsys.webclient.view.NotedEntityView
import java.time.LocalDateTime

data class SolutionVerdictView(
    override val id: Long?,
    override val note: String,
    override val name: String,
    override val creationDate: LocalDateTime?,
    override val additionalInfo: String,
    val score: Long,
    val judgeFullName: String?,
    val taskFullName: String?,
    val contestFullName: String?
) : NotedEntityView<SolutionVerdict> {

    override fun toEntity(timeZoneId: String?): SolutionVerdict {
        TODO("Not yet implemented")
    }

    val formattedCreationDate = creationDate?.format()

    companion object {

        fun SolutionVerdict.toView(timeZone: String?) = SolutionVerdictView(
            id = this.id,
            note = this.note,
            name = this.name,
            creationDate = this.creationDate?.atTimeZone(timeZone),
            additionalInfo = this.additionalInfo,
            taskFullName = this.taskFullMame,
            contestFullName = this.contestFullMame,
            judgeFullName = this.judgeFullMame,
            score = this.score
        )
    }
}
