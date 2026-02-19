package trik.testsys.webapp.backoffice.data.service.impl

import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.Task
import trik.testsys.webapp.backoffice.data.entity.impl.User
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

    override fun findByDeveloper(developer: User): Set<Task> {
        return repository.findByDeveloper(developer)
    }

    override fun findForUser(user: User): Set<Task> {
        val ownedByUser = repository.findByDeveloper(user)
        val viaGroups = if (user.memberedGroups.isEmpty()) emptySet() else repository.findDistinctByUserGroupsIn(user.memberedGroups)
        return (ownedByUser + viaGroups).toSet()
    }

    override fun findAllTesting(): List<Task> {
        val allTesting = repository.findByTestingStatus(Task.TestingStatus.TESTING)
        return allTesting
    }
}