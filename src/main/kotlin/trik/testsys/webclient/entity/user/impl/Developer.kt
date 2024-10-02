package trik.testsys.webclient.entity.user.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.entity.impl.Contest
import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.entity.user.WebUser
import trik.testsys.webclient.enums.UserType
import javax.persistence.*

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Entity
@Table(name = "${TABLE_PREFIX}_DEVELOPER")
class Developer(
    name: String,
    accessToken: AccessToken
) : WebUser(name, accessToken, UserType.DEVELOPER) {

    @OneToMany(mappedBy = "developer", cascade = [CascadeType.ALL])
    val tasks: MutableSet<Task> = mutableSetOf()

    @OneToMany(mappedBy = "developer", cascade = [CascadeType.ALL])
    val contests: MutableSet<Contest> = mutableSetOf()
}