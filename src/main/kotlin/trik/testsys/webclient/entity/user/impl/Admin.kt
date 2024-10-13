package trik.testsys.webclient.entity.user.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.entity.user.WebUser
import javax.persistence.*

@Entity
@Table(name = "${TABLE_PREFIX}_ADMIN")
class Admin(
    name: String,
    accessToken: AccessToken,
) : WebUser(name, accessToken, UserType.ADMIN) {

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    @ManyToOne
    @JoinColumn(
        nullable = false, unique = false, updatable = false,
        name = "viewer_id", referencedColumnName = "id"
    )
    lateinit var viewer: Viewer

    @OneToMany(mappedBy = "admin", fetch = FetchType.EAGER)
    val groups: MutableSet<Group> = mutableSetOf()

//
//    @ManyToMany(mappedBy = "admins")
//    val tasks: MutableSet<Task> = mutableSetOf()
}
