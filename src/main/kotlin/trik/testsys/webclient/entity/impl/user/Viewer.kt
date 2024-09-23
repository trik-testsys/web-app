package trik.testsys.webclient.entity.impl.user

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.entity.user.AbstractUser
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.utils.marker.TrikEntity
import javax.persistence.*

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Entity
@Table(name = "${TABLE_PREFIX}_VIEWER")
class Viewer(
    override var name: String,
    override var accessToken: AccessToken,

    @Column(nullable = false, columnDefinition = "VARCHAR(100)")
    var adminRegToken: String
) : AbstractUser(name, accessToken), TrikEntity {

    @OneToMany(mappedBy = "viewer", cascade = [CascadeType.ALL])
    val admins: MutableSet<Admin> = mutableSetOf()
}