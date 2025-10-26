package trik.testsys.webapp.backoffice.service.analysis.polygon

import trik.testsys.webapp.backoffice.service.analysis.polygon.impl.PolygonParser


sealed interface PolygonDiagnosticReportSeverity {
    object Error : PolygonDiagnosticReportSeverity {
        override fun toString() = "Error"
    }

    object Warning : PolygonDiagnosticReportSeverity {
        override fun toString() = "Warning"
    }
}

data class PolygonDiagnosticReport(
    val diagnosticName: String,
    val location: String?,
    val element: PolygonElement?,
    val severity: PolygonDiagnosticReportSeverity,
    val description: String
) {

    companion object {

        @JvmStatic
        fun fromPolygonParsingException(exception: PolygonParser.PolygonParsingException) =
            PolygonDiagnosticReport(
                "Parsing",
                null,
                null,
                PolygonDiagnosticReportSeverity.Error,
                exception.message ?: "Синтаксическая ошибка в полигоне"
            )

        @JvmStatic
        fun fromException(exception: Exception) = when (exception) {
            is PolygonParser.PolygonParsingException -> fromPolygonParsingException(exception)
            else -> PolygonDiagnosticReport(
                "Unknown",
                null,
                null,
                PolygonDiagnosticReportSeverity.Error,
                exception.message ?: "Ошибка в полигоне"
            )
        }
    }
}