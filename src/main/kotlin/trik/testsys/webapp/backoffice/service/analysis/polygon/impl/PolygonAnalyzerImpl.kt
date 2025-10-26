package trik.testsys.webapp.backoffice.service.analysis.polygon.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.getBeansOfType
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.PolygonFile
import trik.testsys.webapp.backoffice.service.FileManager
import trik.testsys.webapp.backoffice.service.analysis.polygon.Polygon
import trik.testsys.webapp.backoffice.service.analysis.polygon.PolygonAnalyzer
import trik.testsys.webapp.backoffice.service.analysis.polygon.PolygonDiagnostic
import trik.testsys.webapp.backoffice.service.analysis.polygon.PolygonDiagnosticReport
import trik.testsys.webapp.backoffice.service.analysis.polygon.PolygonElement
import trik.testsys.webapp.notifier.CombinedIncidentNotifier
import java.io.File
import java.util.IdentityHashMap

@Service
class PolygonAnalyzerImpl(
    context: ApplicationContext,

    private val incidentNotifier: CombinedIncidentNotifier,
    private val fileManager: FileManager
) : PolygonAnalyzer {

    private val diagnostics = context.getBeansOfType<PolygonDiagnostic>()

    override fun analyze(file: File): List<PolygonDiagnosticReport> {
        logger.debug("Performing analyze(file=${file.name})")

        if (file.isDirectory || !file.exists()) {
            logger.error("Cannot perform analyze: file(${file.name}) does not exist.")
            return emptyList()
        }

        val text = try {
            file.readText()
        } catch (e: Exception) {
            logger.error("Cannot perform analyze: caught exception", e)
            return emptyList()
        }

        return analyze(text)
    }

    override fun analyze(text: String): List<PolygonDiagnosticReport> {
        logger.debug("Performing analyze(text.length=${text.length})")

        val parser = PolygonParser()
        val polygon: Polygon
        val locations: IdentityHashMap<PolygonElement, String>

        when (val parsedData = (parser.tryParse(text))) {
            is ParsedData.SuccessData -> {
                polygon = parsedData.polygon
                locations = parsedData.locations
            }
            is ParsedData.PolygonErrorData -> {
                val report = PolygonDiagnosticReport.fromPolygonParsingException(parsedData.exception)
                return listOf(report)
            }
            is ParsedData.UnknownErrorData -> {
                val report = PolygonDiagnosticReport.fromException(parsedData.exception)
                return listOf(report)
            }
        }

        val allResults = mutableListOf<PolygonDiagnosticReport>()
        diagnostics.forEach { (_, diagnostic) ->
            val results = diagnostic.perform(polygon).map {
                it.element ?: return@map it
                it.copy(location = locations[it.element])
            }

            allResults.addAll(results)
        }

        logger.debug("Analyze performed successful. Analyzed for ${allResults.size} diagnostic reports: $allResults")
        return allResults
    }

    override fun analyze(polygonFile: PolygonFile): List<PolygonDiagnosticReport> {
        logger.debug("Performing analyse(polygonFile.id=${polygonFile.id}).")

        val file = fileManager.getPolygonFile(polygonFile) ?: error("Polygon file is not accessible")
        val results = analyze(file)

        logger.debug("Analyze performed successful(polygonFile.id=${polygonFile.id}).")
        return results
    }

    private fun PolygonParser.tryParse(text: String): ParsedData {
        val polygon = try {
            this.parse(text)
        } catch (e: PolygonParser.PolygonParsingException) {
            logger.error("Cannot parse polygon: caught exception", e)
//            incidentNotifier.notify("Caught PolygonParsingException(message=${e.message}) while parsing polygon: \n text")

            return ParsedData.PolygonErrorData(e)
        } catch (e: Exception) {
            logger.error("Cannot parse polygon: caught unknown exception", e)
            incidentNotifier.notify("Caught unknown exception(className=${e::class.java}, message=${e.message}) while parsing polygon: \n text")

            return ParsedData.UnknownErrorData(e)
        }

        val locations = this.getLocations()

        return ParsedData.SuccessData(polygon, locations)
    }

    sealed interface ParsedData {

        class SuccessData(
            val polygon: Polygon,
            val locations: IdentityHashMap<PolygonElement, String>
        ) : ParsedData

        class PolygonErrorData(
            val exception: PolygonParser.PolygonParsingException
        ) : ParsedData

        class UnknownErrorData(
            val exception: Exception
        ) : ParsedData
    }

    companion object {

        private val logger = LoggerFactory.getLogger(PolygonAnalyzerImpl::class.java)
    }
}