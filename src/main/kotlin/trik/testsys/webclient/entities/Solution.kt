package trik.testsys.webclient.entities

import trik.testsys.webclient.enums.SolutionsStatuses
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
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    val id: Long? = null

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    var countOfTests: Long = 0L

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    var countOfPassedTests: Long = 0L

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    var status: SolutionsStatuses = SolutionsStatuses.NOT_STARTED

    @Column(nullable = false)
    var date: Date = Date()
}