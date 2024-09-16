package trik.testsys.webclient.entity.impl

import trik.testsys.core.entity.user.AbstractUser
import trik.testsys.core.utils.marker.TrikEntity
import javax.persistence.*

@Entity
@Table(name = "TS_ADMIN")
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