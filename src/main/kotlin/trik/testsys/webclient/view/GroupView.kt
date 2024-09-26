package trik.testsys.webclient.view

import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.view.View
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.util.atTimeZone
import java.time.LocalDateTime
import java.util.*

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
data class GroupView(
    override val id: Long?,
    override val creationDate: LocalDateTime?,
    val name: String,
    val accessToken: AccessToken,
    val admin: AdminView? = null
): View<Group> {

    override fun toEntity(timeZone: TimeZone) = Group(
        name, accessToken
    ).also {
        it.id = id
    }

    companion object {

        fun Group.toView(timeZone: TimeZone) = GroupView(
            id = this.id,
            creationDate = this.creationDate?.atTimeZone(timeZone),
            name = this.name,
            accessToken = this.accessToken
        )

        fun GroupView.withAdmin(adminView: AdminView) = copy(
            admin = adminView
        )
    }
}
