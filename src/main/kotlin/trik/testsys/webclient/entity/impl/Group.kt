package trik.testsys.webclient.entity.impl

import javax.persistence.*

@Entity
@Table(name = "GROUPZ")
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

    @Column(
        nullable = false,
        columnDefinition = "BIGINT DEFAULT 100"
    ) var studentsLimit: Long = 100L
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

    @ManyToMany
    @JoinTable(
        name = "GROUPS_BY_LABELS",
        joinColumns = [JoinColumn(name = "group_id")],
        inverseJoinColumns = [JoinColumn(name = "label_id")]
    )
    val labels = mutableSetOf<Label>()

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