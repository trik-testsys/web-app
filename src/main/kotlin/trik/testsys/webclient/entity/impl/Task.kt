package trik.testsys.webclient.entity.impl

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "TASKS")
class Task(
    @Column(nullable = false, columnDefinition = "VARCHAR(50) DEFAULT ''")
    var name: String,

    @Column(nullable = false, columnDefinition = "VARCHAR(200) DEFAULT ''")
    var description: String,

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
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

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    var hasBenchmark: Boolean = false

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    var hasTraining: Boolean = false

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    @OneToMany(mappedBy = "task", cascade = [CascadeType.ALL])
    lateinit var trikFiles: MutableSet<TrikFile>

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    @ManyToMany(cascade = [CascadeType.ALL])
    @JoinTable(
        name = "TASKS_BY_ADMINS",
        joinColumns = [JoinColumn(name = "task_id")],
        inverseJoinColumns = [JoinColumn(name = "admin_id")]
    )
    lateinit var admins: MutableSet<Admin>

    @Column(nullable = true)
    var deadline: LocalDateTime? = null

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    fun getFullName() = "$id: $name"
}