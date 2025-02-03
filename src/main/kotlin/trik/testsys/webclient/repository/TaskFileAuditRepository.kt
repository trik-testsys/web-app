package trik.testsys.webclient.repository

import trik.testsys.core.repository.EntityRepository
import trik.testsys.webclient.entity.impl.TaskFile
import trik.testsys.webclient.entity.impl.TaskFileAudit

/**
 * @author Roman Shishkin
 * @since 2.1.0
 */
interface TaskFileAuditRepository : EntityRepository<TaskFileAudit> {

    fun findAllByTaskFile(taskFile: TaskFile): List<TaskFileAudit>
}