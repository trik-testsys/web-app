//package trik.testsys.webapp.backoffice.data.entity.impl
//
//import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
//import trik.testsys.backoffice.entity.AbstractNotedEntity
//import trik.testsys.backoffice.entity.User
//import jakarta.persistence.Column
//import jakarta.persistence.Entity
//import jakarta.persistence.JoinColumn
//import jakarta.persistence.ManyToOne
//import jakarta.persistence.Table
//import jakarta.persistence.Transient
//
//@Entity
//@Table(name = "${TABLE_PREFIX}_SOLUTION_VERDICT")
//class SolutionVerdict(
//    name: String,
//
//    @ManyToOne
//    @JoinColumn(
//        nullable = false, unique = false, updatable = false,
//        name = "judge_id", referencedColumnName = "id"
//    )
//    val judge: User,
//
//    @ManyToOne
//    @JoinColumn(
//        nullable = false, unique = false, updatable = false,
//        name = "student_id", referencedColumnName = "id"
//    )
//    val student: User,
//
//    @ManyToOne
//    @JoinColumn(
//        nullable = false, unique = false, updatable = false,
//        name = "task_id", referencedColumnName = "id"
//    )
//    val task: Task,
//
//    @ManyToOne
//    @JoinColumn(
//        nullable = true, unique = false, updatable = false,
//        name = "contest_id", referencedColumnName = "id"
//    )
//    val contest: Contest?,
//
//    @Column(nullable = false, unique = false, updatable = true)
//    var score: Long
//) : AbstractNotedEntity(name) {
//
//    @get:Transient
//    val judgeFullMame: String
//        get() = "${judge.id}: ${judge.name}"
//
//    @get:Transient
//    val studentFullMame: String
//        get() = "${student.id}: ${student.name}"
//
//    @get:Transient
//    val taskFullMame: String
//        get() = "${task.id}: ${task.name}"
//
//    @get:Transient
//    val contestFullMame: String
//        get() = "${contest?.id}: ${contest?.name}"
//}