package trik.testsys.webapp.backoffice.service.impl

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import trik.testsys.webapp.backoffice.data.entity.impl.PolygonDiagnosticReportEntity
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.PolygonFile
import trik.testsys.webapp.backoffice.data.service.PolygonDiagnosticReportEntityService
import trik.testsys.webapp.backoffice.data.service.impl.taskFile.PolygonFileService
import trik.testsys.webapp.backoffice.service.analysis.polygon.PolygonAnalyzer

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
class PolygonFileScheduleAnalyzer(
    private val polygonFileService: PolygonFileService,
    private val polygonDiagnosticReportEntityService: PolygonDiagnosticReportEntityService,
    private val polygonAnalyzer: PolygonAnalyzer,

    private val transactionTemplate: TransactionTemplate
) {

    @Scheduled(fixedRate = 10_000)
    fun analyzePolygonFiles() {
        logger.debug("Started analyzePolygonFiles")
        val notAnalyzed = polygonFileService.findNotAnalyzed()

        logger.debug("Found ${notAnalyzed.size} polygonFiles to analyze")
        notAnalyzed.forEach { polygonFile ->
            logger.debug("Started analyzePolygonFiles for polygonFile(id=${polygonFile.id})")

            polygonFile.analysisStatus = PolygonFile.AnalysisStatus.ANALYZING
            polygonFileService.save(polygonFile)

            polygonDiagnosticReportEntityService.findActiveByPolygonFileId(requireNotNull(polygonFile.id)).forEach { result ->
                polygonDiagnosticReportEntityService.delete(result)
            }

            performAnalysis(requireNotNull(polygonFile.id))
        }

        logger.debug("Finished analyzePolygonFiles")
    }

    private fun performAnalysis(polygonFileId: Long) = transactionTemplate.execute {
        val polygonFile = polygonFileService.findById(polygonFileId) ?: run {
                logger.warn("PolygonFile(id=$polygonFileId) not found inside transaction, skipping analysis")
                return@execute
            }

        val results = polygonAnalyzer.analyze(polygonFile).ifEmpty {
            logger.debug("Finished analyzePolygonFiles for polygonFile(id=${polygonFile.id}), setting analysisStatus to 'SUCCESS'. No reports where generated.")

            polygonFile.analysisStatus = PolygonFile.AnalysisStatus.SUCCESS
            polygonFileService.save(polygonFile)
            return@execute
        }

        logger.debug("Finished analyzePolygonFiles for polygonFile(id=${polygonFile.id}), setting analysisStatus to 'FAILED'. ${results.size} reports where generated: $results")

        val analyzeEntities = results.map { result ->
            PolygonDiagnosticReportEntity
                .from(result)
                .with(polygonFile)
        }
        polygonDiagnosticReportEntityService.saveAll(analyzeEntities)

        polygonFile.analysisStatus = PolygonFile.AnalysisStatus.FAILED
        polygonFileService.save(polygonFile)
    }

    companion object {

        private val logger = LoggerFactory.getLogger(PolygonFileScheduleAnalyzer::class.java)
    }
}