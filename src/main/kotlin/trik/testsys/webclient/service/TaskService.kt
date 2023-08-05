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

    fun getAllGroupTasks(groupId: Long): Set<Task>? {
        val group = groupService.getGroupById(groupId) ?: return null

        return group.tasks
    }

    fun getTaskById(taskId: Long): Task? {
        return taskRepository.findTaskById(taskId)
    }
}