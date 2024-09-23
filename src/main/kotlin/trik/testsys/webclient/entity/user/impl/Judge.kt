package trik.testsys.webclient.entity.user.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.utils.marker.TrikEntity
import trik.testsys.webclient.entity.user.WebUser
import trik.testsys.webclient.enums.UserType
//import trik.testsys.webclient.entity.impl.SolutionAction
import javax.persistence.*


@Entity
@Table(name = "${TABLE_PREFIX}_JUDGE")
class Judge(
    override var name: String,
    override var accessToken: AccessToken
) : WebUser(name, accessToken), TrikEntity {

    /**
     * @author Roman Shishkin
     * @since 2.0.0
     **/
    override val type = UserType.JUDGE
//    @OneToMany(mappedBy = "judge", cascade = [CascadeType.ALL])
//    val solutionActions: MutableSet<SolutionAction> = mutableSetOf()
}