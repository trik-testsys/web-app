package trik.testsys.webclient.entity.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.entity.user.AbstractUser
import trik.testsys.core.utils.marker.TrikEntity
import javax.persistence.*

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Entity
@Table(name = "${TABLE_PREFIX}_VIEWER")
class Viewer(
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "web_user_id",
        referencedColumnName = "id",
        nullable = false,
        unique = true
    ) val webUser: WebUser,

    @Column(nullable = false, columnDefinition = "VARCHAR(100)")
    var adminRegToken: String
) : AbstractUser(webUser.name, webUser.accessToken), TrikEntity {

    @OneToMany(mappedBy = "viewer", cascade = [CascadeType.ALL])
    val admins: MutableSet<Admin> = mutableSetOf()
}