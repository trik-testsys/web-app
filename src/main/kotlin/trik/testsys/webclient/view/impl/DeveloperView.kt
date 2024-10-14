package trik.testsys.webclient.view.impl

import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.view.user.UserView
import trik.testsys.webclient.entity.user.impl.Developer
import trik.testsys.webclient.util.fromTimeZone
import java.time.LocalDateTime

data class DeveloperView(
    override val id: Long?,
    override val name: String,
    override val accessToken: AccessToken,
    override val lastLoginDate: LocalDateTime?,
    override val creationDate: LocalDateTime?,
    override val additionalInfo: String,
    val contests: List<ContestView> = emptyList(),
    val tasks: List<TaskView> = emptyList(),
    val polygons: List<TaskFileView> = emptyList(),
    val exercises: List<TaskFileView> = emptyList(),
    val solutions: List<TaskFileView> = emptyList(),
    val conditions: List<TaskFileView> = emptyList()
) : UserView<Developer> {

    override fun toEntity(timeZoneId: String?) = Developer(
        name, accessToken
    ).also {
        it.id = id
        it.lastLoginDate = lastLoginDate?.fromTimeZone(timeZoneId)
        it.additionalInfo = additionalInfo
    }
}
