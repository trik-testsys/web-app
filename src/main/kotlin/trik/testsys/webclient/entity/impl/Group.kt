package trik.testsys.webclient.entity.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.webclient.entity.user.impl.Admin
import trik.testsys.webclient.entity.user.impl.Student
import javax.persistence.*

@Entity
@Table(name = "${TABLE_PREFIX}_GROUP")
class Group(
    @ManyToOne
    @JoinColumn(
        name = "admin_id", referencedColumnName = "id",
        nullable = false
    ) val admin: Admin,

    @Column(
        nullable = false, unique = false, length = 50,
        columnDefinition = "VARCHAR(50) DEFAULT ''"
    ) var name: String,

    @Column(
        nullable = false, unique = true, length = 50,
        columnDefinition = "VARCHAR(100) DEFAULT ''"
    ) val accessToken: String,
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    val id: Long? = null

    @OneToMany(mappedBy = "group", cascade = [CascadeType.ALL])
    val students: MutableSet<Student> = mutableSetOf()

    @ManyToMany
    @JoinTable(
        name = "TASKS_BY_GROUPS",
        joinColumns = [JoinColumn(name = "group_id")],
        inverseJoinColumns = [JoinColumn(name = "task_id")]
    )
    lateinit var tasks: MutableSet<Task>

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    var isAccessible: Boolean = true

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    var isRegistrationOpen: Boolean = true

    @Column(nullable = true, columnDefinition = "VARCHAR(1000)")
    var additionalInfo: String? = null

    constructor(
        admin: Admin,
        name: String,
        accessToken: String,
        additionalInfo: String?
    ) : this(admin, name, accessToken) {
        this.additionalInfo = additionalInfo
    }
}