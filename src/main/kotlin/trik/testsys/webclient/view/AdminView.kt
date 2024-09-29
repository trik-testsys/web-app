package trik.testsys.webclient.view

import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.view.user.UserView
import trik.testsys.webclient.entity.user.impl.Admin
import trik.testsys.webclient.entity.user.impl.Viewer
import trik.testsys.webclient.util.fromTimeZone
import java.time.LocalDateTime
import java.util.*

data class AdminView(
    override val id: Long?,
    override val name: String,
    override val accessToken: AccessToken,
    override val creationDate: LocalDateTime?,
    override val lastLoginDate: LocalDateTime?,
    val viewer: Viewer,
    val additionalInfo: String,
    val groups: List<GroupView>?
) : UserView<Admin> {

    override fun toEntity(timeZone: TimeZone) = Admin(
        name, accessToken
    ).also {
        it.id = id
        it.lastLoginDate = lastLoginDate?.fromTimeZone(timeZone)
        it.additionalInfo = additionalInfo
    }
}