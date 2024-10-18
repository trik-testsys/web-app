package trik.testsys.webclient.entity.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.webclient.entity.AbstractNotedEntity
import trik.testsys.webclient.entity.user.impl.Judge
import trik.testsys.webclient.entity.user.impl.Student
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.Transient

@Entity
@Table(name = "${TABLE_PREFIX}_SOLUTION_VERDICT")
class SolutionVerdict(
    name: String,

    @ManyToOne
    @JoinColumn(
        nullable = false, unique = false, updatable = false,
        name = "judge_id", referencedColumnName = "id"
    )
    val judge: Judge,

    @ManyToOne
    @JoinColumn(
        nullable = false, unique = false, updatable = false,
        name = "student_id", referencedColumnName = "id"
    )
    val student: Student,

    @ManyToOne
    @JoinColumn(
        nullable = false, unique = false, updatable = false,
        name = "task_id", referencedColumnName = "id"
    )
    val task: Task,

    @ManyToOne
    @JoinColumn(
        nullable = true, unique = false, updatable = false,
        name = "contest_id", referencedColumnName = "id"
    )
    val contest: Contest?,

    @Column(nullable = false, unique = false, updatable = true)
    var score: Long
) : AbstractNotedEntity(name) {

    @get:Transient
    val judgeFullMame: String
        get() = "${judge.id}: ${judge.name}"

    @get:Transient
    val studentFullMame: String
        get() = "${student.id}: ${student.name}"

    @get:Transient
    val taskFullMame: String
        get() = "${task.id}: ${task.name}"

    @get:Transient
    val contestFullMame: String
        get() = "${contest?.id}: ${contest?.name}"
}