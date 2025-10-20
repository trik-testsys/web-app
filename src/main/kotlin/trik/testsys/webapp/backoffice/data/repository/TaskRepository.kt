package trik.testsys.webapp.backoffice.data.repository

import org.springframework.stereotype.Repository
import trik.testsys.webapp.backoffice.data.entity.impl.Task
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.entity.impl.UserGroup
import trik.testsys.webapp.core.data.repository.EntityRepository


@Repository
interface TaskRepository : EntityRepository<Task> {

    fun findByDeveloper(developer: User): Set<Task>

    fun findDistinctByUserGroupsIn(userGroups: Collection<UserGroup>): Set<Task>

    fun findByTestingStatus(testingStatus: Task.TestingStatus): List<Task>
}