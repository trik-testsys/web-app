//package trik.testsys.webapp.backoffice.data.repository
//
//import org.springframework.stereotype.Repository
//import trik.testsys.core.repository.named.NamedEntityRepository
//import trik.testsys.backoffice.entity.impl.SolutionVerdict
//import trik.testsys.backoffice.entity.impl.Task
//import trik.testsys.backoffice.entity.user.impl.Judge
//import trik.testsys.backoffice.entity.user.impl.Student
//
//@Repository
//interface SolutionVerdictRepository : NamedEntityRepository<SolutionVerdict> {
//
//    fun findByJudgeAndStudent(judge: Judge, student: Student): List<SolutionVerdict>
//
//    fun findByStudent(student: Student): List<SolutionVerdict>
//
//    fun findByStudentAndTask(student: Student, task: Task): List<SolutionVerdict>
//}