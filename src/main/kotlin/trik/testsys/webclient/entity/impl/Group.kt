package trik.testsys.webclient.entity.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.entity.named.AbstractNamedEntity
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.entity.RegEntity
import trik.testsys.webclient.entity.user.impl.Admin
import trik.testsys.webclient.entity.user.impl.Student
import javax.persistence.*

@Entity
@Table(name = "${TABLE_PREFIX}_GROUP")
class Group(
    name: String,

    @Column(
        nullable = false, unique = true, updatable = false,
        length = RegEntity.REG_TOKEN_LENGTH
    ) override val regToken: AccessToken
) : AbstractNamedEntity(name), RegEntity {

    @ManyToOne
    @JoinColumn(
        nullable = false, unique = false, updatable = false,
        name = "admin_id", referencedColumnName = "id",
    )
    lateinit var admin: Admin

    @OneToMany(mappedBy = "group", cascade = [CascadeType.ALL])
    val students: MutableSet<Student> = mutableSetOf()

    @ManyToMany(mappedBy = "groups", cascade = [CascadeType.ALL])
    val contests: MutableSet<Contest> = mutableSetOf()
}