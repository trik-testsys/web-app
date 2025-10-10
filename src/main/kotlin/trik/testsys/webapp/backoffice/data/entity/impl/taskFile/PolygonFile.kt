package trik.testsys.webapp.backoffice.data.entity.impl.taskFile

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import trik.testsys.webapp.backoffice.data.entity.AbstractFile
import trik.testsys.webapp.backoffice.data.enums.FileType
import trik.testsys.webapp.core.data.entity.AbstractEntity.Companion.TABLE_PREFIX

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Entity
@Table(name = "${TABLE_PREFIX}polygon_file")
class PolygonFile : AbstractFile() {

    @Column(name = "timeLimit", nullable = false)
    var timeLimit: Long = -1

    override var type: FileType? = FileType.XML

    override fun getFileName(version: Long) = "$FILE_NAME_PREFIX-$id-$fileVersion${type?.extension}"

    companion object {

        const val FILE_NAME_PREFIX = "pol"
    }
}