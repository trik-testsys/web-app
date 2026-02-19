package trik.testsys.webapp.backoffice.service.analysis.polygon.impl

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.service.analysis.polygon.AbstractPolygonDiagnostic
import trik.testsys.webapp.backoffice.service.analysis.polygon.Polygon
import trik.testsys.webapp.backoffice.service.analysis.polygon.PolygonDiagnosticReport
import trik.testsys.webapp.backoffice.service.analysis.polygon.TimeLimitConstraint

/**
 * @author Viktor Karasev
 * @since %CURRENT_VERSION%
 */
@Service
class TimeLimitPolygonDiagnostic(
    @Value("\${trik.testsys.diagnostics-availability.polygon.timelimit}")
    private val isAvailable: Boolean
) : AbstractPolygonDiagnostic() {

    override fun isAvailable() = isAvailable

    override fun doPerform(polygon: Polygon): List<PolygonDiagnosticReport> {
        val results = mutableListOf<PolygonDiagnosticReport>()

        val timeLimitConstraints = polygon.constraints.constraints.filterIsInstance<TimeLimitConstraint>().ifEmpty {
            results.addError("Отсутствует ограничение по времени.")
            return results
        }

        if (timeLimitConstraints.size > 1) {
            results.addWarning("Найдено ${timeLimitConstraints.size} ограничения по времени (timelimit), ожидалось 1")
        }

        timeLimitConstraints.forEach {
            if (it.milliseconds < 0 || it.milliseconds > 10 * 60 * 1000) {
                results.addError(it, "Некорректное значение ограничения по времени")
            }
        }

        return results
    }
}