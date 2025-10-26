package trik.testsys.webapp.backoffice.data.repository

import org.springframework.stereotype.Repository
import trik.testsys.webapp.backoffice.data.entity.impl.PolygonDiagnosticReportEntity
import trik.testsys.webapp.core.data.repository.EntityRepository

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Repository
interface PolygonDiagnosticReportEntityRepository : EntityRepository<PolygonDiagnosticReportEntity> {

    fun findByPolygonFileIdAndIsRemoved(polygonFileId: Long?, isRemoved: Boolean = false): List<PolygonDiagnosticReportEntity>
}