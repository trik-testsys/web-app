package trik.testsys.webapp.backoffice.service.startup.runner.impl

import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.PolygonFile
import trik.testsys.webapp.backoffice.data.service.impl.taskFile.PolygonFileService
import trik.testsys.webapp.core.service.startup.AbstractStartupRunner

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
@Service
class PolygonAnalysisRestart(
    private val polygonFileService: PolygonFileService,
) : AbstractStartupRunner() {

    override suspend fun execute() {
        val analysingPolygons = polygonFileService.findAnalyzing()

        logger.debug("Found ${analysingPolygons.size} analysing polygons, from previous session.")
        analysingPolygons.forEach { polygon ->
            polygon.analysisStatus = PolygonFile.AnalysisStatus.NOT_ANALYZED
        }

        polygonFileService.saveAll(analysingPolygons)
    }
}