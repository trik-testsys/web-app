package trik.testsys.webclient.entity.impl.user

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.entity.user.AbstractUser
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.utils.marker.TrikEntity
//import trik.testsys.webclient.entity.impl.Task
import javax.persistence.*

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Entity
@Table(name = "${TABLE_PREFIX}_DEVELOPER")
class Developer(
    override var name: String,
    override var accessToken: AccessToken
) : AbstractUser(name, accessToken), TrikEntity {

//    @OneToMany(mappedBy = "developer", cascade = [CascadeType.ALL])
//    val tasks: MutableSet<Task> = mutableSetOf()
}