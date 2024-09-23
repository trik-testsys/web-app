package trik.testsys.webclient.entity.impl.user

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.entity.user.AbstractUser
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.utils.marker.TrikEntity
import trik.testsys.webclient.entity.impl.Group
//import trik.testsys.webclient.entity.impl.Task
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
) : AbstractUser(name, accessToken), TrikEntity {

    @OneToMany(mappedBy = "admin", cascade = [CascadeType.ALL])
    val groups: MutableSet<Group> = mutableSetOf()
//
//    @ManyToMany(mappedBy = "admins")
//    val tasks: MutableSet<Task> = mutableSetOf()
}
