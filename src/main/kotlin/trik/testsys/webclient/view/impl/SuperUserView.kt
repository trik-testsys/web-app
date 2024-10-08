package trik.testsys.webclient.view.impl

import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.view.user.UserView
import trik.testsys.webclient.entity.user.impl.SuperUser
import trik.testsys.webclient.entity.user.impl.Viewer
import trik.testsys.webclient.util.fromTimeZone
import java.time.LocalDateTime
import java.util.*

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
data class SuperUserView(
    override val id: Long?,
    override val name: String,
    override val accessToken: AccessToken,
    override val creationDate: LocalDateTime?,
    override val lastLoginDate: LocalDateTime?,
    override val additionalInfo: String,
    val viewers: List<Viewer>?
) : UserView<SuperUser> {

    override fun toEntity(timeZone: TimeZone) = SuperUser(
        name, accessToken
    ).also {
        it.id = id
        it.lastLoginDate = lastLoginDate?.fromTimeZone(timeZone)
        it.additionalInfo = additionalInfo
    }
}