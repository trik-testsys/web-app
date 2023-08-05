package trik.testsys.webclient.entity

import javax.persistence.*


@Entity
@Table(name = "TASKS")
class Task(
    @Column(nullable = false, columnDefinition = "VARCHAR(50) DEFAULT ''")
    val name: String,

    @Column(nullable = false, columnDefinition = "VARCHAR(200) DEFAULT ''")
    val description: String,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "developer_id", referencedColumnName = "id",
        nullable = false
    ) val developer: Developer
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    val id: Long? = null

    @ManyToMany(mappedBy = "tasks", cascade = [CascadeType.ALL])
    val groups: MutableSet<Group> = mutableSetOf()

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    var countOfTests: Long = 0L

    @OneToMany(mappedBy = "task", cascade = [CascadeType.ALL])
    val solutions: MutableSet<Solution> = mutableSetOf()

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    var hasBenchmark: Boolean = false

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    var hasTraining: Boolean = false

    @ManyToMany(cascade = [CascadeType.ALL])
    @JoinTable(
        name = "TASKS_BY_ADMINS",
        joinColumns = [JoinColumn(name = "task_id")],
        inverseJoinColumns = [JoinColumn(name = "admin_id")]
    )
    lateinit var admins: MutableSet<Admin>
}