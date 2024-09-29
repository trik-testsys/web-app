package trik.testsys.webclient.entity.user.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.entity.user.AccessToken
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
    name: String,
    accessToken: AccessToken,

    @Column(nullable = false, columnDefinition = "VARCHAR(100)")
    var regToken: String
) : WebUser(name, accessToken, UserType.SUPER_USER) {

    @OneToMany(mappedBy = "viewer", cascade = [CascadeType.ALL])
    val admins: MutableSet<Admin> = mutableSetOf()
}