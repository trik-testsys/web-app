package trik.testsys.webclient.entity.impl.user

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.entity.user.AbstractUser
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.utils.marker.TrikEntity
import trik.testsys.webclient.entity.impl.Group
//import trik.testsys.webclient.entity.impl.Solution
//import trik.testsys.webclient.entity.impl.TaskAction
import javax.persistence.*

@Entity
@Table(name = "${TABLE_PREFIX}_STUDENT")
class Student(
    override var name: String,
    override var accessToken: AccessToken,

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    val group: Group
) : AbstractUser(name, accessToken), TrikEntity {

//    @OneToMany(mappedBy = "student", cascade = [CascadeType.ALL])
//    val solutions: MutableSet<Solution> = mutableSetOf()
//
//    @OneToMany(mappedBy = "student", cascade = [CascadeType.ALL])
//    val taskActions: MutableSet<TaskAction> = mutableSetOf()
}