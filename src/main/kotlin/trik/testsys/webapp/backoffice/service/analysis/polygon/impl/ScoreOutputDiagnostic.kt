package trik.testsys.webapp.backoffice.service.analysis.polygon.impl

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.service.analysis.polygon.AbstractPolygonDiagnostic
import trik.testsys.webapp.backoffice.service.analysis.polygon.MessageTrigger
import trik.testsys.webapp.backoffice.service.analysis.polygon.Polygon
import trik.testsys.webapp.backoffice.service.analysis.polygon.PolygonDiagnosticReport

/**
 * @author Viktor Karasev
 * @since %CURRENT_VERSION%
 */
@Service
class ScoreOutputDiagnostic(
    @Value("\${trik.testsys.diagnostics-availability.polygon.score-output}")
    private val isAvailable: Boolean
) : AbstractPolygonDiagnostic() {

    override fun isAvailable() = isAvailable

    override fun doPerform(polygon: Polygon): List<PolygonDiagnosticReport> {
        val results = mutableListOf<PolygonDiagnosticReport>()

        val prints = polygon
            .atomicTriggers()
            .filterIsInstance<MessageTrigger>()

        val hasScoreOutput = prints
            .any { it.message.contains(SCORE_OUTPUT_FORMAT) }

        if (!hasScoreOutput) {
            results.addWarning("В полигоне отсутствует вывод баллов в формате \"$SCORE_OUTPUT_FORMAT\"")
        }

        return results
    }

    companion object {

        private const val SCORE_OUTPUT_FORMAT = "Набрано баллов:"
    }
}