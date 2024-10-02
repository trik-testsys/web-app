package trik.testsys.webclient.service.entity.impl

import org.springframework.stereotype.Service
import trik.testsys.core.service.named.AbstractNamedEntityService
import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.entity.user.impl.Developer
import trik.testsys.webclient.repository.TaskRepository

@Service
class TaskService : AbstractNamedEntityService<Task, TaskRepository>() {

    fun findByDeveloper(developer: Developer) = repository.findByDeveloper(developer)
}