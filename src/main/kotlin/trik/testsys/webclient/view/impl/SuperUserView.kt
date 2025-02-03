package trik.testsys.webclient.view.impl

import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.view.user.UserView
import trik.testsys.webclient.entity.impl.EmergencyMessage
import trik.testsys.webclient.entity.user.impl.Developer
import trik.testsys.webclient.entity.user.impl.Judge
import trik.testsys.webclient.entity.user.impl.SuperUser
import trik.testsys.webclient.entity.user.impl.Viewer
import trik.testsys.webclient.util.fromTimeZone
import java.time.LocalDateTime

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
    val viewers: List<Viewer> = emptyList(),
    val developers: List<Developer> = emptyList(),
    val judges: List<Judge> = emptyList(),
    val emergencyMessages: List<EmergencyMessage> = emptyList()
) : UserView<SuperUser> {

    override fun toEntity(timeZoneId: String?) = SuperUser(
        name, accessToken
    ).also {
        it.id = id
        it.lastLoginDate = lastLoginDate?.fromTimeZone(timeZoneId)
        it.additionalInfo = additionalInfo
    }
}