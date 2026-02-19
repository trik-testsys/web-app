package trik.testsys.webapp.backoffice.data.service

import trik.testsys.webapp.backoffice.data.entity.impl.PolygonDiagnosticReportEntity
import trik.testsys.webapp.core.data.service.EntityService

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
interface PolygonDiagnosticReportEntityService : EntityService<PolygonDiagnosticReportEntity> {

    fun findActiveByPolygonFileId(polygonFileId: Long): List<PolygonDiagnosticReportEntity>
}