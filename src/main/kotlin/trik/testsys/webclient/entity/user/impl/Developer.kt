package trik.testsys.webclient.entity.user.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.utils.marker.TrikEntity
import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.entity.user.WebUser
import trik.testsys.webclient.enums.UserType
//import trik.testsys.webclient.entity.Task
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
) : WebUser(name, accessToken), TrikEntity {

    /**
     * @author Roman Shishkin
     * @since 2.0.0
    **/
    override val type = UserType.DEVELOPER
    @OneToMany(mappedBy = "developer", cascade = [CascadeType.ALL])
    val tasks: MutableSet<Task> = mutableSetOf()
}