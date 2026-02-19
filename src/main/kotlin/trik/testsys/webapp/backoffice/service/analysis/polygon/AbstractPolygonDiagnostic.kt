package trik.testsys.webapp.backoffice.service.analysis.polygon

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Viktor Karasev
 * @since 3.12.0
 */
abstract class AbstractPolygonDiagnostic : PolygonDiagnostic {

    private val name = this.javaClass.simpleName
    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)

    final override fun perform(polygon: Polygon): List<PolygonDiagnosticReport> {
        logger.debug("Performing diagnostics(name=$name)")

        if (!isAvailable()) {
            logger.debug("Diagnostic(name=$name) is unavailable.")
            return emptyList()
        }

        val reports = doPerform(polygon)
        logger.debug("Diagnostic(name=$name) performed successfully. Diagnosed ${reports.size} reports: $reports.")

        return reports
    }

    abstract fun isAvailable(): Boolean

    abstract fun doPerform(polygon: Polygon): List<PolygonDiagnosticReport>

    protected fun MutableList<PolygonDiagnosticReport>.addWarning(description: String) = add(
        PolygonDiagnosticReport(
            name,
            null,
            null,
            PolygonDiagnosticReportSeverity.Warning,
            description
        )
    )

    protected fun MutableList<PolygonDiagnosticReport>.addError(description: String) = add(
        PolygonDiagnosticReport(
            name,
            null,
            null,
            PolygonDiagnosticReportSeverity.Error,
            description
        )
    )

    protected fun MutableList<PolygonDiagnosticReport>.addWarning(element: PolygonElement, description: String) = add(
        PolygonDiagnosticReport(
            name,
            null,
            element,
            PolygonDiagnosticReportSeverity.Warning,
            description
        )
    )

    protected fun MutableList<PolygonDiagnosticReport>.addError(element: PolygonElement, description: String) = add(
        PolygonDiagnosticReport(
            name,
            null,
            element,
            PolygonDiagnosticReportSeverity.Error,
            description
        )
    )

    protected fun Polygon.atomicTriggers(): List<PolygonAtomicTrigger> {
        val eventTriggers =
            this.constraints.constraints
                .filterIsInstance<EventConstraint>()
                .flatMap { it.trigger.atomicTriggers }
        val initTriggers =
            this.constraints.constraints
                .filterIsInstance<InitConstraint>()
                .flatMap { it.triggers }
        return eventTriggers + initTriggers
    }

    protected fun Polygon.conditions(): List<Condition> {
        val eventConditions =
            this.constraints.constraints
                .filterIsInstance<EventConstraint>()
                .flatMap { it.condition.conditions }

        val constraintConditions =
            this.constraints.constraints
                .filterIsInstance<PlainConstraint>()
                .flatMap { it.condition.conditions }

        return eventConditions + constraintConditions
    }

    private fun Condition.atomicConditions(): List<AtomicCondition> {
        return when (this) {
            is DroppedCondition -> listOf(this)
            is InsideCondition -> listOf(this)
            is SettedUpCondition -> listOf(this)
            is TimerCondition -> listOf(this)
            is TrueCondition -> listOf(this)
            is BinaryCondition -> listOf(this)
            is NotCondition -> this.condition.atomicConditions()
            is PolygonCondition -> this.conditions.flatMap { it.atomicConditions() }
        }
    }

    protected fun Polygon.atomicConditions(): List<AtomicCondition> {
        return this.conditions().flatMap { it.atomicConditions() }
    }
}