package trik.testsys.webapp.backoffice.data.entity.impl.taskFile

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Table
import trik.testsys.webapp.backoffice.data.entity.AbstractFile
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.enums.FileType
import trik.testsys.webapp.core.data.entity.AbstractEntity.Companion.TABLE_PREFIX

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
@Entity
@Table(name = "${TABLE_PREFIX}solution_file")
class SolutionFile : AbstractFile() {

    override fun getFileName(version: Long) = "$FILE_NAME_PREFIX-$id-$version${type?.extension}"

    @Convert(converter = Solution.SolutionType.Companion.SolutionTypeConverter::class)
    @Column(name = "solution_type", nullable = false)
    var solutionType: Solution.SolutionType = Solution.SolutionType.QRS

    companion object {

        const val FILE_NAME_PREFIX = "sol"

        val allowedTypes = setOf(FileType.QRS, FileType.PYTHON, FileType.JAVASCRIPT)
    }
}