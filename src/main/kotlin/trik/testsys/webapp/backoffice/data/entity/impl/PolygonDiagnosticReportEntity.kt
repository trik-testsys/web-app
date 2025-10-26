package trik.testsys.webapp.backoffice.data.entity.impl

import jakarta.persistence.Column
import jakarta.persistence.Converter
import jakarta.persistence.Entity
import jakarta.persistence.Table
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.PolygonFile
import trik.testsys.webapp.backoffice.service.analysis.polygon.PolygonDiagnosticReport
import trik.testsys.webapp.backoffice.service.analysis.polygon.PolygonDiagnosticReportSeverity
import trik.testsys.webapp.core.data.entity.AbstractEntity
import trik.testsys.webapp.core.data.entity.AbstractEntity.Companion.TABLE_PREFIX
import trik.testsys.webapp.core.utils.enums.PersistableEnum
import trik.testsys.webapp.core.utils.enums.converter.AbstractPersistableEnumConverter


/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Entity
@Table(name = "${TABLE_PREFIX}polygon_diagnostic_report")
class PolygonDiagnosticReportEntity(
    @Column(name = "polygon_file_id", nullable = false)
    val polygonFileId: Long,

    @Column(name = "diagnostic_name", nullable = false)
    val diagnosticName: String,
    @Column(name = "level", nullable = false)
    val level: Level,
    @Column(name = "description", nullable = false)
    val description: String,
    @Column(name = "location", nullable = true, length = 4000)
    val location: String? = null
) : AbstractEntity() {

    @Column(name = "is_removed", nullable = false)
    var isRemoved: Boolean = false

    class Builder internal constructor() {

        private var polygonFile: PolygonFile? = null
        private var polygonDiagnosticReport: PolygonDiagnosticReport? = null

        fun polygonFile(polygonFile: PolygonFile) = apply {
            this.polygonFile = polygonFile
        }

        fun polygonDiagnosticReport(polygonDiagnosticReport: PolygonDiagnosticReport) = apply {
            this.polygonDiagnosticReport = polygonDiagnosticReport
        }

        fun with(polygonFile: PolygonFile) = this
            .polygonFile(polygonFile)
            .build()

        fun build() = PolygonDiagnosticReportEntity(
            polygonFileId = requireNotNull(polygonFile?.id),

            diagnosticName = requireNotNull(polygonDiagnosticReport).diagnosticName,
            level = when (requireNotNull(polygonDiagnosticReport).severity) {
                PolygonDiagnosticReportSeverity.Error -> Level.ERROR
                PolygonDiagnosticReportSeverity.Warning -> Level.WARNING
            },
            description = requireNotNull(polygonDiagnosticReport).description,
            location = requireNotNull(polygonDiagnosticReport).location
        )
    }

    enum class Level(override val dbKey: String, val locale: String) : PersistableEnum {
        WARNING("W", "Предупреждение"),
        ERROR("E", "Ошибка");

        companion object {

            @Converter(autoApply = true)
            class EnumConverter : AbstractPersistableEnumConverter<Level>()
        }
    }

    companion object {

        @JvmStatic
        fun from(polygonDiagnosticReport: PolygonDiagnosticReport): Builder {
            val builder = Builder()
            return builder.polygonDiagnosticReport(polygonDiagnosticReport)
        }
    }
}