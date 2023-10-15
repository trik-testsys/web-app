package trik.testsys.webclient.entity.impl

import trik.testsys.webclient.entity.TrikEntity
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import javax.persistence.*

@Entity
@Table(name = "TASK_ACTIONS")
class TaskAction(
    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "student_id", referencedColumnName = "id", nullable = false)
    val student: Student,

    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    val dateTime: LocalDateTime = LocalDateTime.now(UTC),
) : TrikEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    val id: Long? = null

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "task_id", referencedColumnName = "id", nullable = true)
    var task: Task? = null

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "solution_id", referencedColumnName = "id", nullable = true)
    var solution: Solution? = null

    @Column(nullable = false, columnDefinition = "VARCHAR(50) DEFAULT ''")
    var type: Type? = null

    constructor(student: Student, task: Task) : this(student) {
        this.task = task
        this.type = Type.DOWNLOADED_TRAINING
    }

    constructor(student: Student, solution: Solution) : this(student) {
        this.solution = solution
        this.type = Type.UPLOADED_SOLUTION
    }

    enum class Type {
        DOWNLOADED_TRAINING,
        UPLOADED_SOLUTION
    }
}