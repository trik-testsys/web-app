//package trik.testsys.webapp.backoffice.data.service.impl
//
//import org.springframework.stereotype.Service
//import trik.testsys.core.service.named.AbstractNamedEntityService
//import trik.testsys.backoffice.entity.impl.SolutionVerdict
//import trik.testsys.backoffice.entity.impl.Task
//import trik.testsys.backoffice.entity.user.impl.Judge
//import trik.testsys.backoffice.entity.user.impl.Student
//import trik.testsys.backoffice.repository.SolutionVerdictRepository
//
//@Service
//class SolutionVerdictService : AbstractNamedEntityService<SolutionVerdict, SolutionVerdictRepository>() {
//
//    fun findByJudgeAndStudent(judge: Judge, student: Student) = repository.findByJudgeAndStudent(judge, student)
//
//    fun findByStudent(student: Student) = repository.findByStudent(student)
//
//    fun findByStudentAndTask(student: Student, task: Task) = repository.findByStudentAndTask(student, task)
//}