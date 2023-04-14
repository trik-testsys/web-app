package trik.testsys.webclient.entities

import javax.persistence.*


@Entity
@Table(name = "TASKS")
class Task(
    @Column(nullable = false, unique = true, columnDefinition = "VARCHAR(50) DEFAULT ''")
    val name: String,

    @Column(nullable = false, columnDefinition = "VARCHAR(200) DEFAULT ''")
    val description: String,
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    val id: Long? = null

    @ManyToMany(mappedBy = "tasks", cascade = [CascadeType.ALL])
    lateinit var groups: MutableSet<Group>

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    var countOfTests: Long = 0L
}