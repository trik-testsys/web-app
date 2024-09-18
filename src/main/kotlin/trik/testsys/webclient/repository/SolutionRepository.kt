package trik.testsys.webclient.repository//package trik.testsys.webclient.repository.impl
//
//import org.springframework.data.repository.CrudRepository
//import org.springframework.stereotype.Repository
//
//import trik.testsys.webclient.entity.impl.Solution
//import trik.testsys.webclient.entity.impl.user.Student
//import trik.testsys.webclient.entity.impl.Task
//
//@Repository
//interface SolutionRepository : CrudRepository<Solution, Long> {
//
//    fun findSolutionsByTask(task: Task): List<Solution>?
//
//    fun findSolutionsByStudent(student: Student): List<Solution>?
//
//    fun findSolutionsByStudentAndTask(student: Student, task: Task): List<Solution>?
//
//    fun countByStudent(student: Student): Long
//}