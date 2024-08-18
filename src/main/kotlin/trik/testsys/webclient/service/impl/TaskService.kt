package trik.testsys.webclient.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

import trik.testsys.webclient.entity.impl.Developer
import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.entity.impl.TrikFile
import trik.testsys.webclient.repository.TaskRepository

@Service
class TaskService @Autowired constructor(
    private val taskRepository: TaskRepository,
    private val groupService: GroupService,
    private val trikFileService: TrikFileService,
) {

    /**
     * @return Saved [Task] if it was saved, [null] otherwise
     * @author Roman Shishkin
     * @since 1.1.0
     */
    fun saveTask(
        name: String,
        description: String,
        developer: Developer,
        tests: List<MultipartFile>,
        training: MultipartFile?,
        benchmark: MultipartFile?,
    ): Task {
        val task = Task(name, description, developer)
        task.countOfTests = tests.size.toLong()
        taskRepository.save(task)

        task.hasBenchmark = benchmark != null
        task.hasTraining = training != null

        val allFiles = tests.map { TrikFile(task, it.originalFilename!!, TrikFile.Type.TEST) }.toMutableSet()

        benchmark ?.let {
            val benchmarkFile = TrikFile(task, benchmark.originalFilename!!, TrikFile.Type.BENCHMARK)
            allFiles.add(benchmarkFile)
        }

        training ?.let {
            val trainingFile = TrikFile(task, training.originalFilename!!, TrikFile.Type.TRAINING)
            allFiles.add(trainingFile)
        }

        task.trikFiles = allFiles
        return taskRepository.save(task)
    }

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    fun saveTask(task: Task): Task {
        return taskRepository.save(task)
    }

    /**
     * @return [Boolean.true] if task was deleted, [Boolean.false] otherwise
     * @author Roman Shishkin
     * @since 1.1.0
     */
    fun deleteTask(taskId: Long): Boolean {
        val task = taskRepository.findTaskById(taskId) ?: return false
        taskRepository.delete(task)

        return true
    }

    /**
     * @return Updated [Task] if it was updated, null it was not found in database.
     * @param newName New name of task
     * @param taskId Id of task
     * @since 1.1.0
     * @author Roman Shishkin
     */
    fun updateName(taskId: Long, newName: String): Task? {
        val task = taskRepository.findTaskById(taskId) ?: return null
        task.name = newName

        return taskRepository.save(task)
    }

    /**
     * @return Updated [Task] if it was updated, null it was not found in database.
     * @since 1.1.0
     * @param newDescription New description of task
     * @param taskId Id of task
     * @author Roman Shiskin
     */
    fun updateDescription(taskId: Long, newDescription: String): Task? {
        val task = taskRepository.findTaskById(taskId) ?: return null
        task.description = newDescription

        return taskRepository.save(task)
    }

    fun getAllGroupTasks(groupId: Long): Set<Task>? {
        val group = groupService.getGroupById(groupId) ?: return null

        return group.tasks
    }

    fun getTaskById(taskId: Long): Task? {
        return taskRepository.findTaskById(taskId)
    }
}