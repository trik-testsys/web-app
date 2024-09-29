package trik.testsys.webclient.entity.user.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.entity.user.WebUser
import trik.testsys.webclient.enums.UserType
import javax.persistence.*

@Entity
@Table(name = "${TABLE_PREFIX}_STUDENT")
class Student(
    name: String,
    accessToken: AccessToken,

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    val group: Group
) : WebUser(name, accessToken, UserType.STUDENT) {

//    @OneToMany(mappedBy = "student", cascade = [CascadeType.ALL])
//    val solutions: MutableSet<Solution> = mutableSetOf()
//
//    @OneToMany(mappedBy = "student", cascade = [CascadeType.ALL])
//    val taskActions: MutableSet<TaskAction> = mutableSetOf()
}