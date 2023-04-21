package trik.testsys.webclient.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import trik.testsys.webclient.entities.Solution

import trik.testsys.webclient.repositories.SolutionRepository
import trik.testsys.webclient.repositories.StudentRepository
import trik.testsys.webclient.repositories.TaskRepository


@Service
class SolutionService {

    @Autowired
    lateinit var solutionRepository: SolutionRepository

    @Autowired
    lateinit var studentRepository: StudentRepository

    @Autowired
    lateinit var taskRepository: TaskRepository

    fun saveSolution(studentId: Long, taskId: Long, gradingId: Long): Solution? {
        val student = studentRepository.findStudentById(studentId) ?: return null
        val task = taskRepository.findTaskById(taskId) ?: return null

        val solution = Solution(student, task, gradingId)
        solution.countOfTests = task.countOfTests
        return solutionRepository.save(solution)
    }

    fun saveSolution(solution: Solution): Solution? {
        return solutionRepository.save(solution)
    }

    fun getAllStudentSolutionsByTask(studentId: Long, taskId: Long): List<Solution>? {
        val student = studentRepository.findStudentById(studentId) ?: return null
        val task = taskRepository.findTaskById(taskId) ?: return null

        return solutionRepository.findSolutionsByStudentAndTask(student, task)
    }

    fun getAllStudentSolutions(studentId: Long): List<Solution>? {
        val student = studentRepository.findStudentById(studentId) ?: return null

        return solutionRepository.findSolutionsByStudent(student)
    }

    fun getAllTaskSolutions(taskId: Long): List<Solution>? {
        val task = taskRepository.findTaskById(taskId) ?: return null

        return solutionRepository.findSolutionsByTask(task)
    }

    fun getAllSolutions(): List<Solution> {
        return solutionRepository.findAll().toList()
    }
}