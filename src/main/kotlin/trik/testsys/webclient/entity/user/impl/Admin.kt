package trik.testsys.webclient.entity.user.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.entity.user.WebUser
import trik.testsys.webclient.enums.UserType
import javax.persistence.*

@Entity
@Table(name = "${TABLE_PREFIX}_ADMIN")
class Admin(
    name: String,
    accessToken: AccessToken,

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    @ManyToOne
    @JoinColumn(
        name = "viewer_id",
        referencedColumnName = "id"
    ) var viewer: Viewer
) : WebUser(name, accessToken, UserType.ADMIN) {

    @OneToMany(mappedBy = "admin", cascade = [CascadeType.ALL])
    val groups: MutableSet<Group> = mutableSetOf()

//
//    @ManyToMany(mappedBy = "admins")
//    val tasks: MutableSet<Task> = mutableSetOf()
}
