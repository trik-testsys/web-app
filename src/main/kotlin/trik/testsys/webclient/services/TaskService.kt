package trik.testsys.webclient.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import trik.testsys.webclient.entities.Task
import trik.testsys.webclient.repositories.TaskRepository

@Service
class TaskService {

    @Autowired
    private lateinit var taskRepository: TaskRepository

    @Autowired
    private lateinit var groupService: GroupService

    fun saveTask(name: String, description: String, groupAccessToken: String, testsCount: Long): Task? {
        val group = groupService.getGroupByAccessToken(groupAccessToken) ?: return null

        TODO()
//        val task = Task(name, description)
//
//        task.countOfTests = testsCount
//
//        task.groups.add(group)
//        group.tasks.add(task)
//
//        return taskRepository.save(task)
    }

    fun getAllGroupTasks(groupId: Long): Set<Task>? {
        val group = groupService.getGroupById(groupId) ?: return null

        return group.tasks
    }

    fun getTaskById(taskId: Long): Task? {
        return taskRepository.findTaskById(taskId)
    }
}