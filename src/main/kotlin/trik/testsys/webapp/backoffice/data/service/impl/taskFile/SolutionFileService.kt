package trik.testsys.webapp.backoffice.data.service.impl.taskFile

import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.ConditionFile
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.SolutionFile
import trik.testsys.webapp.backoffice.data.repository.taskFile.SolutionFileRepository
import trik.testsys.webapp.core.data.service.AbstractService

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
class SolutionFileService : AbstractService<SolutionFile, SolutionFileRepository>() {

    fun finaByDeveloper(developerId: Long, isRemoved: Boolean = false): Set<SolutionFile> {
        val result = repository.findByDeveloperId(developerId).filter { it.isRemoved == isRemoved }
        return result.toSet()
    }
}