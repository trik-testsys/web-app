package trik.testsys.webclient.entity.user.impl

import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.utils.marker.TrikEntity
import trik.testsys.webclient.entity.user.WebUser
import trik.testsys.webclient.enums.UserType
import javax.persistence.*

@Entity
@Table(name = "SUPER_USERS")
class SuperUser(
    override var name: String,
    override var accessToken: AccessToken
): WebUser(name, accessToken), TrikEntity {

    /**
     * @author Roman Shishkin
     * @since 2.0.0
     **/
    override val type = UserType.SUPER_USER
}