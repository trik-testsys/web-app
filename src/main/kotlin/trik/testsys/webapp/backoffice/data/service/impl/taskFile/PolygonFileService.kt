package trik.testsys.webapp.backoffice.data.service.impl.taskFile

import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.PolygonFile
import trik.testsys.webapp.backoffice.data.repository.taskFile.PolygonFileRepository
import trik.testsys.webapp.core.data.service.AbstractService

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
@Service
class PolygonFileService : AbstractService<PolygonFile, PolygonFileRepository>() {

    fun findByDeveloper(developerId: Long, isRemoved: Boolean = false): Set<PolygonFile> {
        val result = repository.findByDeveloperId(developerId).filter { it.isRemoved == isRemoved }
        return result.toSet()
    }

    fun findNotAnalyzed(): List<PolygonFile> {
        val result = repository.findByAnalysisStatus(PolygonFile.AnalysisStatus.NOT_ANALYZED)
        return result
    }

    fun findAnalyzing(): List<PolygonFile> {
        val result = repository.findByAnalysisStatus(PolygonFile.AnalysisStatus.ANALYZING)
        return result
    }
}