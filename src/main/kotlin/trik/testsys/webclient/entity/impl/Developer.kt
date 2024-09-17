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
@Table(name = "${TABLE_PREFIX}_DEVELOPER")
class Developer(
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "web_user_id",
        referencedColumnName = "id",
        nullable = false,
        unique = true
    ) val webUser: WebUser
) : AbstractUser(webUser.name, webUser.accessToken), TrikEntity {

    @OneToMany(mappedBy = "developer", cascade = [CascadeType.ALL])
    val tasks: MutableSet<Task> = mutableSetOf()
}