package trik.testsys.webapp.backoffice.service.analysis.polygon


/**
 * @author Viktor Karasev
 * @since 3.12.0
 */
interface PolygonDiagnostic {

    fun perform(polygon: Polygon): List<PolygonDiagnosticReport>
}