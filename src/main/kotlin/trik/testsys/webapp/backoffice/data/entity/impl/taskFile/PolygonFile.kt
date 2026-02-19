package trik.testsys.webapp.backoffice.data.entity.impl.taskFile

import jakarta.persistence.Column
import jakarta.persistence.Converter
import jakarta.persistence.Entity
import jakarta.persistence.Table
import trik.testsys.webapp.backoffice.data.entity.AbstractFile
import trik.testsys.webapp.backoffice.data.enums.FileType
import trik.testsys.webapp.core.data.entity.AbstractEntity.Companion.TABLE_PREFIX
import trik.testsys.webapp.core.utils.enums.PersistableEnum
import trik.testsys.webapp.core.utils.enums.converter.AbstractPersistableEnumConverter

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
@Entity
@Table(name = "${TABLE_PREFIX}polygon_file")
class PolygonFile : AbstractFile() {

    @Column(name = "timeLimit", nullable = false)
    var timeLimit: Long = -1

    @Column(name = "analysis_status", nullable = false)
    var analysisStatus: AnalysisStatus = AnalysisStatus.NOT_ANALYZED

    override var type: FileType? = FileType.XML

    override fun getFileName(version: Long) = "$FILE_NAME_PREFIX-$id-$version${type?.extension}"

    enum class AnalysisStatus(override val dbKey: String, val locale: String) : PersistableEnum {
        NOT_ANALYZED("NA", "Ожидает"),
        ANALYZING("A", "В процессе"),
        SUCCESS("S", "Ошибки отсутствуют"),
        FAILED("F", "Имеются ошибки");

        companion object {

            @Converter(autoApply = true)
            class EnumConverter : AbstractPersistableEnumConverter<AnalysisStatus>()
        }
    }

    companion object {

        const val FILE_NAME_PREFIX = "pol"

        val allowedTypes = setOf(FileType.XML)
    }
}