package trik.testsys.webclient.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

import trik.testsys.webclient.entity.Developer
import trik.testsys.webclient.entity.Task
import trik.testsys.webclient.entity.TrikFile
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

        benchmark?.let {
            val benchmarkFile = TrikFile(task, benchmark.originalFilename!!, TrikFile.Type.BENCHMARK)
            allFiles.add(benchmarkFile)
        }

        training?.let {
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
     * @author Roman Shishkin
     * @since 1.1.0
     */
    fun update(
        taskId: Long,
        name: String,
        description: String,
        tests: List<MultipartFile>,
        training: MultipartFile?,
        benchmark: MultipartFile?,
    ): Task? {
        val task = taskRepository.findTaskById(taskId) ?: return null

        task.name = name
        task.description = description
        task.countOfTests = tests.size.toLong()
        task.hasBenchmark = benchmark != null
        task.hasTraining = training != null

        val allFiles = tests.map { TrikFile(task, it.originalFilename!!, TrikFile.Type.TEST) }.toMutableSet()

        benchmark?.let {
            val benchmarkFile = TrikFile(task, benchmark.originalFilename!!, TrikFile.Type.BENCHMARK)
            allFiles.add(benchmarkFile)
        }

        training?.let {
            val trainingFile = TrikFile(task, training.originalFilename!!, TrikFile.Type.TRAINING)
            allFiles.add(trainingFile)
        }

        task.trikFiles = allFiles
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