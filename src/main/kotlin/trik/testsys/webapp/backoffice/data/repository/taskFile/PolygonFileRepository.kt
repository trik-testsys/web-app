package trik.testsys.webapp.backoffice.data.repository.taskFile

import org.springframework.stereotype.Repository
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.PolygonFile
import trik.testsys.webapp.core.data.repository.EntityRepository

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Repository
interface PolygonFileRepository : EntityRepository<PolygonFile> {

    fun findByDeveloperId(developerId: Long): Set<PolygonFile>

    fun findByAnalysisStatus(analysisStatus: PolygonFile.AnalysisStatus): List<PolygonFile>
}