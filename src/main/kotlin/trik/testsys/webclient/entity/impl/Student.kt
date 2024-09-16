package trik.testsys.webclient.entity.impl

import trik.testsys.core.entity.user.AbstractUser
import trik.testsys.core.utils.marker.TrikEntity
import javax.persistence.*

@Entity
@Table(name = "TS_STUDENT")
class Student(
    @ManyToOne
    @JoinColumn(name = "web_user_id", nullable = false, unique = true)
    val webUser: WebUser,

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    val group: Group
) : AbstractUser(webUser.name, webUser.accessToken), TrikEntity {

    @OneToMany(mappedBy = "student", cascade = [CascadeType.ALL])
    val solutions: MutableSet<Solution> = mutableSetOf()

    @OneToMany(mappedBy = "student", cascade = [CascadeType.ALL])
    val taskActions: MutableSet<TaskAction> = mutableSetOf()
}