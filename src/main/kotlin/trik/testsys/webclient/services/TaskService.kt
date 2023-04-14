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

    fun saveTask(name: String, description: String, groupAccessToken: String): Task? {
        val group = groupService.getGroupByAccessToken(groupAccessToken) ?: return null
        val task = Task(name, description)

        task.groups.add(group)
        group.tasks.add(task)

        return taskRepository.save(task)
    }
}