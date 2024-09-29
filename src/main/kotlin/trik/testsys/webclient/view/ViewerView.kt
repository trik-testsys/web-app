package trik.testsys.webclient.view

import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.view.user.UserView
import trik.testsys.webclient.entity.user.impl.Viewer
import trik.testsys.webclient.util.fromTimeZone
import java.time.LocalDateTime
import java.util.*

data class ViewerView(
    override val id: Long?,
    override val name: String,
    override val accessToken: AccessToken,
    override val lastLoginDate: LocalDateTime?,
    override val creationDate: LocalDateTime?,
    override val additionalInfo: String,
    val regToken: AccessToken
) : UserView<Viewer> {

    override fun toEntity(timeZone: TimeZone) = Viewer(
        name, accessToken, regToken
    ).also {
        it.id = id
        it.lastLoginDate = lastLoginDate?.fromTimeZone(timeZone)
        it.additionalInfo = additionalInfo
    }
}
