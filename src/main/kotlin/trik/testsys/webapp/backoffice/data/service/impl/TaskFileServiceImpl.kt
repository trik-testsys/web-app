package trik.testsys.webapp.backoffice.data.service.impl

import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.TaskFile
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.repository.TaskFileRepository
import trik.testsys.webapp.backoffice.data.service.TaskFileService
import trik.testsys.webapp.core.data.service.AbstractService

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 **/
@Service
class TaskFileServiceImpl :
    AbstractService<TaskFile, TaskFileRepository>(),
    TaskFileService {

    override fun findByDeveloper(developer: User): Set<TaskFile> =
        repository.findByDeveloper(developer)
}