package trik.testsys.webclient.service.entity.impl

import org.springframework.stereotype.Service
import trik.testsys.core.service.AbstractService
import trik.testsys.webclient.entity.impl.Contest
import trik.testsys.webclient.entity.impl.Solution
import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.entity.user.impl.Student
import trik.testsys.webclient.repository.SolutionRepository

@Service
class SolutionService : AbstractService<Solution, SolutionRepository>() {

    fun findTaskTests(task: Task): List<Solution> {
        val allTaskSolutions = repository.findByTask(task)
        val taskTests = allTaskSolutions.filter { it.isTest }

        return taskTests
    }

    fun findByStudent(student: Student): List<Solution> {
        return repository.findByStudent(student)
    }

    fun findByStudentAndTask(student: Student, task: Task): List<Solution> {
        return repository.findByStudentAndTask(student, task)
    }

    fun findByStudentAndContest(student: Student, contest: Contest): List<Solution> {
        val solutions = mutableListOf<Solution>()
        contest.tasks.forEach { task ->
            val taskSolutions = findByStudentAndTask(student, task)
            solutions.addAll(taskSolutions)
        }

        return solutions
    }
}