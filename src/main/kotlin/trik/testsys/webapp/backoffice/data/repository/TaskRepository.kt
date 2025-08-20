package trik.testsys.webapp.backoffice.data.repository

import org.springframework.stereotype.Repository
import trik.testsys.webapp.backoffice.data.entity.impl.Task
//import trik.testsys.webapp.backoffice.data.entity.impl.TaskTemplate
import trik.testsys.webapp.core.data.repository.EntityRepository


@Repository
interface TaskRepository : EntityRepository<Task> {

//    fun findByCreatedFrom(taskTemplate: TaskTemplate): Set<Task>
}