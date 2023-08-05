package trik.testsys.webclient.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import trik.testsys.webclient.entity.Solution

import trik.testsys.webclient.repository.SolutionRepository
import trik.testsys.webclient.repository.StudentRepository
import trik.testsys.webclient.repository.TaskRepository


@Service
class SolutionService {

    @Autowired
    lateinit var solutionRepository: SolutionRepository

    @Autowired
    lateinit var studentRepository: StudentRepository

    @Autowired
    lateinit var taskRepository: TaskRepository

    fun saveSolution(studentId: Long, taskId: Long, gradingId: Long): Solution? {
        val student = studentRepository.findById(studentId) ?: return null
        val task = taskRepository.findTaskById(taskId) ?: return null

        val solution = Solution(student, task, gradingId)
        solution.countOfTests = task.countOfTests
        return solutionRepository.save(solution)
    }

    fun saveSolution(solution: Solution): Solution? {
        return solutionRepository.save(solution)
    }

    fun getAllStudentSolutionsByTask(studentId: Long, taskId: Long): List<Solution>? {
        val student = studentRepository.findById(studentId) ?: return null
        val task = taskRepository.findTaskById(taskId) ?: return null

        return solutionRepository.findSolutionsByStudentAndTask(student, task)
    }

    fun getAllStudentSolutions(studentId: Long): List<Solution>? {
        val student = studentRepository.findById(studentId) ?: return null

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