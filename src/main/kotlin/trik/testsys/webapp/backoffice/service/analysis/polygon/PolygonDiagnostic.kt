package trik.testsys.webapp.backoffice.service.analysis.polygon


/**
 * @author Viktor Karasev
 * @since %CURRENT_VERSION%
 */
interface PolygonDiagnostic {

    fun perform(polygon: Polygon): List<PolygonDiagnosticReport>
}