package trik.testsys.webclient.entity.user.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.utils.marker.TrikEntity
import trik.testsys.webclient.entity.user.WebUser
import trik.testsys.webclient.enums.UserType
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
) : WebUser(name, accessToken), TrikEntity {

    @OneToMany(mappedBy = "viewer", cascade = [CascadeType.ALL])
    val admins: MutableSet<Admin> = mutableSetOf()

    /**
     * @author Roman Shishkin
     * @since 2.0.0
     **/
    override val type = UserType.VIEWER
}