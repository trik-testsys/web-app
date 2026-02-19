package trik.testsys.webapp.backoffice.data.service

import trik.testsys.webapp.backoffice.data.entity.impl.TaskFile
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.core.data.service.EntityService

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
interface TaskFileService : EntityService<TaskFile> {

    fun findByDeveloper(developer: User): Set<TaskFile>
}