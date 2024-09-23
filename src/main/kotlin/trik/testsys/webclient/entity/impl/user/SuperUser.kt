package trik.testsys.webclient.entity.impl.user

import trik.testsys.core.entity.user.AbstractUser
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.utils.marker.TrikEntity
import javax.persistence.*

@Entity
@Table(name = "SUPER_USERS")
class SuperUser(
    override var name: String,
    override var accessToken: AccessToken
): AbstractUser(name, accessToken), TrikEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    val id: Long? = null
}