package trik.testsys.webclient.entity.user.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.entity.RegEntity
import trik.testsys.webclient.entity.user.WebUser
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

    @Column(
        nullable = false, unique = true, updatable = false,
        length = RegEntity.REG_TOKEN_LENGTH
    )
    override val regToken: AccessToken
) : WebUser(name, accessToken, UserType.VIEWER), RegEntity {

    @OneToMany(mappedBy = "viewer")
    val admins: MutableSet<Admin> = mutableSetOf()
}