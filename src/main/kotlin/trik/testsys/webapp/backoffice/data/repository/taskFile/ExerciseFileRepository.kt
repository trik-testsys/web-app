package trik.testsys.webapp.backoffice.data.repository.taskFile

import org.springframework.stereotype.Repository
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.ExerciseFile
import trik.testsys.webapp.core.data.repository.EntityRepository

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
@Repository
interface ExerciseFileRepository : EntityRepository<ExerciseFile> {

    fun findByDeveloperId(developerId: Long): Set<ExerciseFile>
}