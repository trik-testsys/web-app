package trik.testsys.webapp.backoffice.data.entity.impl

import jakarta.persistence.Column
import jakarta.persistence.Converter
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import trik.testsys.webapp.backoffice.data.entity.impl.TaskFile.TaskFileType.Companion.extension
import trik.testsys.webapp.core.data.entity.AbstractEntity
import trik.testsys.webapp.core.data.entity.AbstractEntity.Companion.TABLE_PREFIX
import trik.testsys.webapp.core.utils.enums.PersistableEnum
import trik.testsys.webapp.core.utils.enums.converter.AbstractPersistableEnumConverter

@Entity
@Table(name = "${TABLE_PREFIX}task_file")
class TaskFile() : AbstractEntity() {

    @Column(name = "name", nullable = false)
    var name: String? = null

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "developer_id", nullable = false)
    var developer: User? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: TaskFileType? = null

    @Column(name = "file_version", nullable = false)
    var fileVersion: Long = 0

    @Transient
    val fileName: String = "$id-$fileVersion${type?.extension()}"

    @ManyToMany(mappedBy = "taskFiles")
    var taskTemplates: MutableSet<TaskTemplate> = mutableSetOf()

    enum class TaskFileType(override val dbKey: String) : PersistableEnum {

        POLYGON("PLG"),
        EXERCISE("EXR"),
        SOLUTION("SLN"),
        CONDITION("CND");

        fun canBeRemovedOnTaskTesting() = this == CONDITION || this == EXERCISE

        companion object {

            @Converter(autoApply = true)
            class TaskFileTypeConverter : AbstractPersistableEnumConverter<TaskFileType>()

            @JvmStatic
            fun TaskFileType.localized() = when(this) {
                POLYGON -> "Полигон"
                EXERCISE -> "Упражнение"
                SOLUTION -> "Решение"
                CONDITION -> "Условие"
            }

            @JvmStatic
            fun TaskFileType.extension() = when(this) {
                POLYGON -> ".xml"
                EXERCISE -> ".qrs"
                SOLUTION -> ".qrs"
                CONDITION -> ".pdf"
            }
        }
    }
}