package trik.testsys.webclient.view.impl

import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.view.user.UserView
import trik.testsys.webclient.entity.user.impl.Admin
import trik.testsys.webclient.entity.user.impl.Viewer
import trik.testsys.webclient.util.fromTimeZone
import java.time.LocalDateTime

data class AdminView(
    override val id: Long?,
    override val name: String,
    override val accessToken: AccessToken,
    override val creationDate: LocalDateTime?,
    override val lastLoginDate: LocalDateTime?,
    override val additionalInfo: String,
    val viewer: Viewer,
    val groups: List<GroupView>? = null
) : UserView<Admin> {

    override fun toEntity(timeZoneId: String?) = Admin(
        name, accessToken
    ).also {
        it.id = id
        it.lastLoginDate = lastLoginDate?.fromTimeZone(timeZoneId)
        it.additionalInfo = additionalInfo
        it.viewer = viewer
    }
}