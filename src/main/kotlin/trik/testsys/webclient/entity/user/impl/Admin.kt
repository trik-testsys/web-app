package trik.testsys.webclient.entity.user.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.utils.marker.TrikEntity
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.entity.user.WebUser
import trik.testsys.webclient.enums.UserType
import javax.persistence.*

@Entity
@Table(name = "${TABLE_PREFIX}_ADMIN")
class Admin(
    override var name: String,
    override var accessToken: AccessToken,

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    @ManyToOne
    @JoinColumn(
        name = "viewer_id",
        referencedColumnName = "id"
    ) var viewer: Viewer
) : WebUser(name, accessToken), TrikEntity {

    @OneToMany(mappedBy = "admin", cascade = [CascadeType.ALL])
    val groups: MutableSet<Group> = mutableSetOf()

    /**
     * @author Roman Shishkin
     * @since 2.0.0
     */
    override val type = UserType.ADMIN
//
//    @ManyToMany(mappedBy = "admins")
//    val tasks: MutableSet<Task> = mutableSetOf()
}
