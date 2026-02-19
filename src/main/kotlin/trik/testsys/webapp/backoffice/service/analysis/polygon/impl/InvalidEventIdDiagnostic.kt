package trik.testsys.webapp.backoffice.service.analysis.polygon.impl

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.service.analysis.polygon.AbstractPolygonDiagnostic
import trik.testsys.webapp.backoffice.service.analysis.polygon.DropTrigger
import trik.testsys.webapp.backoffice.service.analysis.polygon.DroppedCondition
import trik.testsys.webapp.backoffice.service.analysis.polygon.EventConstraint
import trik.testsys.webapp.backoffice.service.analysis.polygon.Polygon
import trik.testsys.webapp.backoffice.service.analysis.polygon.PolygonDiagnosticReport
import trik.testsys.webapp.backoffice.service.analysis.polygon.SetUpTrigger
import trik.testsys.webapp.backoffice.service.analysis.polygon.SettedUpCondition

/**
 * @author Viktor Karasev
 * @since 3.12.0
 */
@Service
class InvalidEventIdDiagnostic(
    @Value("\${trik.testsys.diagnostics-availability.polygon.invalid-event-id}")
    private val isAvailable: Boolean
) : AbstractPolygonDiagnostic() {

    override fun isAvailable() = isAvailable

    override fun doPerform(polygon: Polygon): List<PolygonDiagnosticReport> {
        val results = mutableListOf<PolygonDiagnosticReport>()

        val droppedCondition = polygon
            .atomicConditions()
            .filterIsInstance<DroppedCondition>()

        val settedUpCondition = polygon
            .atomicConditions()
            .filterIsInstance<SettedUpCondition>()

        val dropTriggers = polygon
            .atomicTriggers()
            .filterIsInstance<DropTrigger>()

        val setUpTriggers = polygon
            .atomicTriggers()
            .filterIsInstance<SetUpTrigger>()

        for (condition in droppedCondition) {
            if (!eventExist(polygon, condition.eventId)) {
                results.addError(condition, "Событие с id=${condition.eventId} не существует")
            }
        }

        for (condition in settedUpCondition) {
            if (!eventExist(polygon, condition.eventId)) {
                results.addError(condition, "Событие с id=${condition.eventId} не существует")
            }
        }

        for (trigger in dropTriggers) {
            if (!eventExist(polygon, trigger.eventId)) {
                results.addError(trigger, "Событие с id=${trigger.eventId} не существует")
            }
        }

        for (trigger in setUpTriggers) {
            if (!eventExist(polygon, trigger.eventId)) {
                results.addError(trigger, "Событие с id=${trigger.eventId} не существует")
            }
        }

        return results
    }

    private fun eventExist(polygon: Polygon, eventId: String): Boolean {
        return polygon.constraints.constraints.filterIsInstance<EventConstraint>().any { it.identifier == eventId }
    }
}