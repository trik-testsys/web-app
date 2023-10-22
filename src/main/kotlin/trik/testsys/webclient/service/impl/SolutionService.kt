package trik.testsys.webclient.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import trik.testsys.webclient.entity.impl.Solution
import trik.testsys.webclient.entity.impl.Student
import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.entity.impl.TaskAction

import trik.testsys.webclient.repository.impl.SolutionRepository
import trik.testsys.webclient.repository.impl.StudentRepository
import trik.testsys.webclient.repository.impl.TaskRepository
import trik.testsys.webclient.service.TrikService


@Service
class SolutionService @Autowired constructor(
    private val solutionRepository: SolutionRepository,
    private val studentRepository: StudentRepository,
    private val taskRepository: TaskRepository,
    private val taskActionService: TaskActionService
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

    fun countByStudent(student: Student): Long {
        return solutionRepository.countByStudent(student)
    }

    fun getBestSolutionByTaskAndStudent(task: Task, student: Student): Solution? {
        val allTasks = taskRepository.findAll()
        val downloadedTrainingActions = mutableListOf<TaskAction>()
        allTasks.forEach{ task ->
            val taskAction = taskActionService.getDownloadedTrainingAction(student, task)
            if (taskAction != null) downloadedTrainingActions.add(taskAction)
        }
        val startTime = downloadedTrainingActions.maxByOrNull { it.dateTime }?.dateTime ?: return null
        val maxEndTime = startTime.plusSeconds(maxTimeToSolve)

        val solutions = solutionRepository.findSolutionsByStudent(student)?.filter { solution ->
            solution.task == task
        } ?: return null

        val uploadedSolutionActions = mutableListOf<TaskAction>()
        solutions.forEach { solution ->
            val taskAction = taskActionService.getUploadedSolutionAction(student, solution)
            if (taskAction != null) uploadedSolutionActions.add(taskAction)
        }

        val acceptedSolutionActions = uploadedSolutionActions.filter {
            (it.dateTime.isAfter(startTime) || it.dateTime.isEqual(startTime)) &&
            (it.dateTime.isBefore(maxEndTime) || it.dateTime.isEqual(maxEndTime))
        }
        val acceptedSolutions = acceptedSolutionActions.map { it.solution }
        val bestScoreSolution = acceptedSolutions.maxByOrNull { it?.score ?: 0 } ?: return null

        return bestScoreSolution
    }

    companion object {
        private const val maxTimeToSolve = 90 * 60L
    }
}