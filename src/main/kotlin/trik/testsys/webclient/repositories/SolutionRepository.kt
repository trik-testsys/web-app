package trik.testsys.webclient.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

import trik.testsys.webclient.entities.Solution
import trik.testsys.webclient.entities.Student
import trik.testsys.webclient.entities.Task

@Repository
interface SolutionRepository : CrudRepository<Solution, String> {

    fun findSolutionsByTask(task: Task): List<Solution>?

    fun findSolutionsByStudent(student: Student): List<Solution>?

    fun findSolutionsByStudentAndTask(student: Student, task: Task): List<Solution>?
}