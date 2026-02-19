package trik.testsys.webapp.backoffice.service.analysis.polygon

import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.PolygonFile
import java.io.File

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
interface PolygonAnalyzer {

    fun analyze(file: File): List<PolygonDiagnosticReport>

    fun analyze(text: String): List<PolygonDiagnosticReport>

    fun analyze(polygonFile: PolygonFile): List<PolygonDiagnosticReport>
}