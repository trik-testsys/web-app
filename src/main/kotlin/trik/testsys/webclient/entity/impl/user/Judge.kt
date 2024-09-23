package trik.testsys.webclient.entity.impl.user

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.entity.user.AbstractUser
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.utils.marker.TrikEntity
//import trik.testsys.webclient.entity.impl.SolutionAction
import javax.persistence.*


@Entity
@Table(name = "${TABLE_PREFIX}_JUDGE")
class Judge(
    override var name: String,
    override var accessToken: AccessToken
) : AbstractUser(name, accessToken), TrikEntity {

//    @OneToMany(mappedBy = "judge", cascade = [CascadeType.ALL])
//    val solutionActions: MutableSet<SolutionAction> = mutableSetOf()
}