package trik.testsys.webclient.view.impl

import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.view.named.NamedEntityView
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.util.atTimeZone
import java.time.LocalDateTime

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
data class GroupView(
    override val id: Long?,
    override val creationDate: LocalDateTime?,
    override val name: String,
    override val additionalInfo: String,
    val regToken: AccessToken,
    val admin: AdminView? = null
): NamedEntityView<Group> {

    override fun toEntity(timeZoneId: String?) = Group(
        name, regToken
    ).also {
        it.id = id
        it.additionalInfo = additionalInfo
    }

    companion object {

        fun Group.toView(timeZoneId: String?) = GroupView(
            id = this.id,
            creationDate = this.creationDate?.atTimeZone(timeZoneId),
            name = this.name,
            regToken = this.regToken,
            additionalInfo = this.additionalInfo
        )

        fun GroupView.withAdmin(adminView: AdminView) = copy(
            admin = adminView
        )
    }
}
