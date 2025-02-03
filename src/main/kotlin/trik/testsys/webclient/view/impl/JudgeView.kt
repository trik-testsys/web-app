package trik.testsys.webclient.view.impl

import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.view.user.UserView
import trik.testsys.webclient.entity.user.impl.Judge
import trik.testsys.webclient.util.fromTimeZone
import java.time.LocalDateTime

data class JudgeView(
    override val id: Long?,
    override val name: String,
    override val accessToken: AccessToken,
    override val creationDate: LocalDateTime?,
    override val lastLoginDate: LocalDateTime?,
    override val additionalInfo: String,
) : UserView<Judge> {

    override fun toEntity(timeZoneId: String?) = Judge(
        name, accessToken
    ).also {
        it.id = id
        it.lastLoginDate = lastLoginDate?.fromTimeZone(timeZoneId)
        it.additionalInfo = additionalInfo
    }
}