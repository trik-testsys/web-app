package trik.testsys.webapp.backoffice.data.service.impl

import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.Task
import trik.testsys.webapp.backoffice.data.entity.impl.TaskTemplate
import trik.testsys.webapp.backoffice.data.repository.TaskRepository
import trik.testsys.webapp.backoffice.data.service.TaskService
import trik.testsys.webapp.core.data.service.AbstractService

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
class TaskServiceImpl :
    AbstractService<Task, TaskRepository>(),
    TaskService {

    override fun findByTaskTemplate(taskTemplate: TaskTemplate): Set<Task> {
        return repository.findByCreateFrom(taskTemplate)
    }
}