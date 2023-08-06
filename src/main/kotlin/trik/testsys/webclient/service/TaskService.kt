package trik.testsys.webclient.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import trik.testsys.webclient.entity.Developer

import trik.testsys.webclient.entity.Task
import trik.testsys.webclient.repository.TaskRepository

@Service
class TaskService {

    @Autowired
    private lateinit var taskRepository: TaskRepository

    @Autowired
    private lateinit var groupService: GroupService

    /**
     * @return Saved [Task] if it was saved, [null] otherwise
     * @author Roman Shishkin
     * @since 1.1.0
     */
    fun saveTask(
        name: String,
        description: String,
        testsCount: Long,
        developer: Developer,
        training: MultipartFile?,
        benchmark: MultipartFile?,
    ): Task {
        val task = Task(name, description, developer)

        task.countOfTests = testsCount
        task.hasBenchmark = benchmark != null
        task.hasTraining = training != null

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