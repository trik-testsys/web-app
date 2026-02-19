package trik.testsys.webapp.backoffice.data.repository.taskFile

import org.springframework.stereotype.Repository
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.ExerciseFile
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.PolygonFile
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.SolutionFile
import trik.testsys.webapp.core.data.repository.EntityRepository

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
@Repository
interface SolutionFileRepository : EntityRepository<SolutionFile> {

    fun findByDeveloperId(developerId: Long): Set<SolutionFile>
}