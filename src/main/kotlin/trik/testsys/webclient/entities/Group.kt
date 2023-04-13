package trik.testsys.webclient.entities

import javax.persistence.*

@Entity
@Table(name = "GROUPZ")
class Group(
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "admin_id", referencedColumnName = "id",
        nullable = false, unique = true
    ) val admin: Admin,

    @Column(
        nullable = false, unique = true, length = 50,
        columnDefinition = "VARCHAR(50) DEFAULT ''"
    ) val name: String,

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
    lateinit var students: Set<Student>

    @ManyToMany(cascade = [CascadeType.ALL])
    @JoinTable(
        name = "TASKS_BY_GROUPS",
        joinColumns = [JoinColumn(name = "group_id")],
        inverseJoinColumns = [JoinColumn(name = "task_id")]
    )
    lateinit var tasks: Set<Task>
}