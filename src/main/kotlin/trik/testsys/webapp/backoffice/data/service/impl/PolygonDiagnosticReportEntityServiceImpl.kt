package trik.testsys.webapp.backoffice.data.service.impl

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.PolygonDiagnosticReportEntity
import trik.testsys.webapp.backoffice.data.repository.PolygonDiagnosticReportEntityRepository
import trik.testsys.webapp.backoffice.data.service.PolygonDiagnosticReportEntityService
import trik.testsys.webapp.core.data.service.AbstractService

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
class PolygonDiagnosticReportEntityServiceImpl :
    PolygonDiagnosticReportEntityService,
    AbstractService<PolygonDiagnosticReportEntity, PolygonDiagnosticReportEntityRepository>() {

    override fun findActiveByPolygonFileId(polygonFileId: Long): List<PolygonDiagnosticReportEntity> {
        val active = repository.findByPolygonFileIdAndIsRemoved(polygonFileId)
        return active
    }

    override fun delete(entity: PolygonDiagnosticReportEntity) {
        if (entity.isRemoved) {
            return logger.debug("PolygonDiagnosticReportEntity(id=${entity.id}) is already removed.")
        }

        entity.isRemoved = true
        repository.save(entity)
    }

    override fun deleteById(id: Long) {
        repository.findByIdOrNull(id)?.let { toRemove ->
            delete(toRemove)
        } ?: return logger.warn("PolygonDiagnosticReportEntity(id=$id) not found.")
    }

    companion object {

        private val logger = LoggerFactory.getLogger(PolygonDiagnosticReportEntityServiceImpl::class.java)
    }
}