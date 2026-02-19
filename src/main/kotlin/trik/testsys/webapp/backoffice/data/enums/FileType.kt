package trik.testsys.webapp.backoffice.data.enums

import jakarta.persistence.Converter
import trik.testsys.webapp.core.utils.enums.PersistableEnum
import trik.testsys.webapp.core.utils.enums.converter.AbstractPersistableEnumConverter

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
enum class FileType(
    override val dbKey: String,
    val extension: String,
) : PersistableEnum {

    QRS("QRS", ".qrs"),
    PYTHON("PY", ".py"),
    JAVASCRIPT("JS", ".js"),
    XML("XML", ".xml"),
    ZIP("ZIP", ".zip"),
    PDF("PDF", ".pdf"),
    TXT("TXT", ".txt");

    companion object {

        @Converter(autoApply = true)
        class EnumConverter : AbstractPersistableEnumConverter<FileType>()
    }
}