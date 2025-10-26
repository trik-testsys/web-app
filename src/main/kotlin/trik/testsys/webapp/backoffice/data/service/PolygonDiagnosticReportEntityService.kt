package trik.testsys.webapp.backoffice.data.service

import trik.testsys.webapp.backoffice.data.entity.impl.PolygonDiagnosticReportEntity
import trik.testsys.webapp.core.data.service.EntityService

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
interface PolygonDiagnosticReportEntityService : EntityService<PolygonDiagnosticReportEntity> {

    fun findActiveByPolygonFileId(polygonFileId: Long): List<PolygonDiagnosticReportEntity>
}