package trik.testsys.webapp.backoffice.data.service.impl.taskFile

import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.ConditionFile
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.PolygonFile
import trik.testsys.webapp.backoffice.data.repository.taskFile.PolygonFileRepository
import trik.testsys.webapp.core.data.service.AbstractService

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
class PolygonFileService : AbstractService<PolygonFile, PolygonFileRepository>() {

    fun finaByDeveloper(developerId: Long, isRemoved: Boolean = false): Set<PolygonFile> {
        val result = repository.findByDeveloperId(developerId).filter { it.isRemoved == isRemoved }
        return result.toSet()
    }
}