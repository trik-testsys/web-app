package trik.testsys.webclient.entity.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.entity.user.AbstractUser
import trik.testsys.core.utils.marker.TrikEntity
import javax.persistence.*


@Entity
@Table(name = "${TABLE_PREFIX}_JUDGE")
class Judge(
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "web_user_id",
        referencedColumnName = "id",
        nullable = false,
        unique = true
    ) val webUser: WebUser
) : AbstractUser(webUser.name, webUser.accessToken), TrikEntity {

    @OneToMany(mappedBy = "judge", cascade = [CascadeType.ALL])
    val solutionActions: MutableSet<SolutionAction> = mutableSetOf()
}