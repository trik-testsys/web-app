package trik.testsys.webclient.entity

import java.util.Date
import javax.persistence.*

@Entity
@Table(name = "SOLUTIONS")
class Solution(
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    val student: Student,

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    val task: Task,

    @Column(nullable = false, unique = true)
    val gradingId: Long
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    val id: Long? = null

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    var countOfTests: Long = 0L

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    var countOfPassedTests: Long = 0L

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 3")
    var status: Status = Status.NOT_STARTED

    @Column(nullable = false)
    var date: Date = Date()

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    enum class Status {
        FAILED,
        PASSED,
        IN_PROGRESS,
        NOT_STARTED,
        ERROR
    }
}