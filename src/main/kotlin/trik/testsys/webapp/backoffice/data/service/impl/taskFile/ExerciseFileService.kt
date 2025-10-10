package trik.testsys.webapp.backoffice.data.service.impl.taskFile

import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.ConditionFile
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.ExerciseFile
import trik.testsys.webapp.backoffice.data.repository.taskFile.ExerciseFileRepository
import trik.testsys.webapp.core.data.service.AbstractService

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
class ExerciseFileService : AbstractService<ExerciseFile, ExerciseFileRepository>() {

    fun finaByDeveloper(developerId: Long, isRemoved: Boolean = false): Set<ExerciseFile> {
        val result = repository.findByDeveloperId(developerId).filter { it.isRemoved == isRemoved }
        return result.toSet()
    }
}