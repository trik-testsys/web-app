package trik.testsys.webclient.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import trik.testsys.webclient.entity.impl.Solution
import trik.testsys.webclient.entity.impl.Student
import trik.testsys.webclient.entity.impl.Task

import trik.testsys.webclient.repository.impl.SolutionRepository
import trik.testsys.webclient.repository.impl.StudentRepository
import trik.testsys.webclient.repository.impl.TaskRepository
import trik.testsys.webclient.service.TrikService


@Service
class SolutionService @Autowired constructor(
    private val solutionRepository: SolutionRepository,
    private val studentRepository: StudentRepository,
    private val taskRepository: TaskRepository
) : TrikService {

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

    fun getBestSolutionByTaskAndStudent(task: Task, student: Student): Solution? {
        return solutionRepository.findSolutionsByStudentAndTask(student, task)?.maxByOrNull { it.score }
    }
}