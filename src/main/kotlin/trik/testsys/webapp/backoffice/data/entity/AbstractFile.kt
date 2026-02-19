package trik.testsys.webapp.backoffice.data.entity

import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Enumerated
import jakarta.persistence.ManyToMany
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Transient
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import trik.testsys.webapp.backoffice.data.entity.impl.Task
import trik.testsys.webapp.backoffice.data.enums.FileType
import trik.testsys.webapp.core.data.entity.AbstractEntity
import trik.testsys.webapp.core.utils.enums.PersistableEnum

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@MappedSuperclass
abstract class AbstractFile : AbstractEntity() {

    @Column(name = "name", nullable = false)
    var name: String? = null

    @Column(name = "data", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    var data: Data = Data()

    @Column(name = "developer_id", nullable = false)
    var developerId: Long? = null

    @Column(name = "file_version", nullable = false)
    var fileVersion: Long = 0

    @Column(name = "is_removed", nullable = false)
    var isRemoved: Boolean = false

    @Convert(converter = FileType.Companion.EnumConverter::class)
    @Column(name = "type", nullable = false)
    var type: FileType? = null

    abstract fun getFileName(version: Long = fileVersion): String

    @get:Transient
    val taskIds: List<Long>
        get() = data.attachedTaskIds

    data class Data(
        val originalFileNameByVersion: MutableMap<Long, String> = mutableMapOf(),
        val attachedTaskIds: MutableList<Long> = mutableListOf(),
    )
}