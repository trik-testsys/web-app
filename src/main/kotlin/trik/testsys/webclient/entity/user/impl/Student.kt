package trik.testsys.webclient.entity.user.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.entity.user.WebUser
import javax.persistence.*

@Entity
@Table(name = "${TABLE_PREFIX}_STUDENT")
class Student(
    name: String,
    accessToken: AccessToken
) : WebUser(name, accessToken, UserType.STUDENT) {

    @ManyToOne
    @JoinColumn(
        nullable = false, unique = false, updatable = false,
        name = "group_id", referencedColumnName = "id"
    )
    lateinit var group: Group

//    @OneToMany(mappedBy = "student", cascade = [CascadeType.ALL])
//    val solutions: MutableSet<Solution> = mutableSetOf()
//
//    @OneToMany(mappedBy = "student", cascade = [CascadeType.ALL])
//    val taskActions: MutableSet<TaskAction> = mutableSetOf()
}