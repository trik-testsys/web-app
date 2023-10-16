package trik.testsys.webclient.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import trik.testsys.webclient.entity.impl.Solution
import trik.testsys.webclient.entity.impl.Student
import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.entity.impl.TaskAction
import trik.testsys.webclient.repository.impl.TaskActionRepository
import trik.testsys.webclient.service.TrikService

@Service
class TaskActionService @Autowired constructor(
    private val taskActionRepository: TaskActionRepository
): TrikService {

    fun save(student: Student, task: Task): TaskAction {
        val taskAction = TaskAction(student, task)
        return taskActionRepository.save(taskAction)
    }

    fun save(student: Student, solution: Solution): TaskAction {
        val taskAction = TaskAction(student, solution)
        return taskActionRepository.save(taskAction)
    }

    fun getDownloadedTrainingAction(student: Student, task: Task): TaskAction? {
        val allActions = taskActionRepository.findAllByStudentAndTask(student, task)

        return allActions?.firstOrNull { it.type == TaskAction.Type.DOWNLOADED_TRAINING }
    }

    fun getUploadedSolutionAction(student: Student, solution: Solution): TaskAction? {
        val allActions = taskActionRepository.findAllByStudentAndSolution(student, solution)

        return allActions?.firstOrNull { it.type == TaskAction.Type.UPLOADED_SOLUTION }
    }
}