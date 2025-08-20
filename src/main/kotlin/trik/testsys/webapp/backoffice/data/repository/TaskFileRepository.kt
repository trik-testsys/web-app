package trik.testsys.webapp.backoffice.data.repository

import org.springframework.stereotype.Repository
import trik.testsys.webapp.backoffice.data.entity.impl.TaskFile
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.core.data.repository.EntityRepository

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 **/
@Repository
interface TaskFileRepository : EntityRepository<TaskFile> {

    fun findByDeveloper(developer: User): Set<TaskFile>

    fun findByDeveloperAndType(developer: User, type: TaskFile.TaskFileType): List<TaskFile>
}