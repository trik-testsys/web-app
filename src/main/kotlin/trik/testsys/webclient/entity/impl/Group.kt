package trik.testsys.webclient.entity.impl

import trik.testsys.core.entity.AbstractEntity
import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.entity.user.impl.Admin
import trik.testsys.webclient.entity.user.impl.Student
import javax.persistence.*

@Entity
@Table(name = "${TABLE_PREFIX}_GROUP")
class Group(
    @Column(
        nullable = false, unique = false, length = 50,
        columnDefinition = "VARCHAR(50) DEFAULT ''"
    ) var name: String,

    @Column(
        nullable = false, unique = true, length = 50,
        columnDefinition = "VARCHAR(100) DEFAULT ''"
    ) val regToken: AccessToken,
) : AbstractEntity() {

    @ManyToOne
    @JoinColumn(
        name = "admin_id", referencedColumnName = "id",
        nullable = false
    ) var admin: Admin? = null

    @OneToMany(mappedBy = "group", cascade = [CascadeType.ALL])
    val students: MutableSet<Student> = mutableSetOf()

    @ManyToMany
    @JoinTable(
        name = "TASKS_BY_GROUPS",
        joinColumns = [JoinColumn(name = "group_id")],
        inverseJoinColumns = [JoinColumn(name = "task_id")]
    )
    lateinit var tasks: MutableSet<Task>
}