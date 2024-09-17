package trik.testsys.webclient.entity.impl.user

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.entity.user.AbstractUser
import trik.testsys.core.utils.marker.TrikEntity
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.entity.impl.Task
import javax.persistence.*

@Entity
@Table(name = "${TABLE_PREFIX}_ADMIN")
class Admin(
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "web_user_id",
        referencedColumnName = "id",
        nullable = false,
        unique = true
    ) val webUser: WebUser
) : AbstractUser(webUser.name, webUser.accessToken), TrikEntity {

    @OneToMany(mappedBy = "admin", cascade = [CascadeType.ALL])
    val groups: MutableSet<Group> = mutableSetOf()

    @ManyToMany(mappedBy = "admins")
    val tasks: MutableSet<Task> = mutableSetOf()

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    @ManyToOne
    @JoinColumn(
        name = "viewer_id",
        referencedColumnName = "id"
    ) lateinit var viewer: Viewer

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    constructor(webUser: WebUser, viewer: Viewer) : this(webUser) {
        this.viewer = viewer
    }
}