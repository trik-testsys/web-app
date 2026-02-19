package trik.testsys.webapp.backoffice.data.entity.impl.taskFile

import jakarta.persistence.Entity
import jakarta.persistence.Table
import trik.testsys.webapp.backoffice.data.entity.AbstractFile
import trik.testsys.webapp.backoffice.data.enums.FileType
import trik.testsys.webapp.core.data.entity.AbstractEntity.Companion.TABLE_PREFIX

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
@Entity
@Table(name = "${TABLE_PREFIX}exercise_file")
class ExerciseFile : AbstractFile() {

    override fun getFileName(version: Long) = "$FILE_NAME_PREFIX-$id-$version${type?.extension}"

    companion object {

        const val FILE_NAME_PREFIX = "ex"

        val allowedTypes = setOf(FileType.QRS, FileType.ZIP)
    }
}