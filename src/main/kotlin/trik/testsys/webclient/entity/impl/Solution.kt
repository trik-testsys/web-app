package trik.testsys.webclient.entity.impl

import trik.testsys.core.entity.AbstractEntity
import trik.testsys.webclient.entity.user.impl.Student
import javax.persistence.*

@Entity
@Table(name = "TRIK_SOLUTION")
class Solution(
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    val student: Student,

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    val task: Task
) : AbstractEntity() {

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    var countOfTests: Long = 0L

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    var countOfPassedTests: Long = 0L

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 3")
    var status: Status = Status.NOT_STARTED

    @Column(nullable = false)
    var score: Long = 0

//    @OneToMany(mappedBy = "solution", cascade = [CascadeType.ALL])
//    val solutionActions: MutableSet<SolutionAction> = mutableSetOf()

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    enum class Status {
        FAILED,
        PASSED,
        IN_PROGRESS,
        NOT_STARTED,
        ERROR,
        PARTIAL
    }
}