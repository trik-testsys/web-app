package trik.testsys.webclient.entity.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.utils.enums.Enum
import trik.testsys.core.utils.enums.converter.AbstractEnumConverter
import trik.testsys.webclient.entity.AbstractNotedEntity
import trik.testsys.webclient.entity.user.impl.Developer
import javax.persistence.*

@Entity
@Table(name = "${TABLE_PREFIX}_TASK_FILE")
class TaskFile(
    name: String,

    @Column(nullable = false, unique = false, updatable = false)
    val type: TaskFileType
) : AbstractNotedEntity(name) {

    @ManyToOne
    @JoinColumn(
        nullable = false, unique = false, updatable = false,
        name = "developer_id", referencedColumnName = "id"
    )
    lateinit var developer: Developer


    enum class TaskFileType(override val dbkey: String) : Enum {

        POLYGON("PLG"),
        EXERCISE("EXR"),
        SOLUTION("SLN");

        companion object {

            @Converter(autoApply = true)
            class TaskFileTypeConverter : AbstractEnumConverter<TaskFileType>()

            fun TaskFileType.toL10nMessage() = when(this) {
                POLYGON -> "Полигон"
                EXERCISE -> "Упражнение"
                SOLUTION -> "Решение"
            }
        }
    }

    companion object {

        fun polygon(name: String) = TaskFile(name, TaskFileType.POLYGON)

        fun exercise(name: String) = TaskFile(name, TaskFileType.EXERCISE)

        fun solution(name: String) = TaskFile(name, TaskFileType.SOLUTION)
    }
}