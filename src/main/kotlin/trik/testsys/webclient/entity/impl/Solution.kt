package trik.testsys.webclient.entity.impl

import trik.testsys.core.entity.AbstractEntity
import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.utils.enums.Enum
import trik.testsys.core.utils.enums.converter.AbstractEnumConverter
import trik.testsys.webclient.entity.user.impl.Student
import javax.persistence.*

@Entity
@Table(name = "${TABLE_PREFIX}_SOLUTION")
class Solution(
    @Column(nullable = false, unique = false, updatable = false)
    val type: SolutionType,
) : AbstractEntity() {

    @ManyToOne
    @JoinColumn(
        nullable = false, unique = false, updatable = false,
        name = "task_id", referencedColumnName = "id"
    )
    lateinit var task: Task

    @ManyToOne
    @JoinColumn(
        nullable = true, unique = false, updatable = false,
        name = "student_id", referencedColumnName = "id"
    )
    var student: Student? = null

    @get:Transient
    val isTest: Boolean
        get() = student == null

    @Column(nullable = false, unique = false, updatable = true)
    var status: SolutionStatus = SolutionStatus.NOT_STARTED

    @Column(nullable = false, unique = false, updatable = true)
    var score: Long = -1

    fun isScored() = score != -1L

    // solutions should be linked not only with tasks but also with contests

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    enum class SolutionStatus(override val dbkey: String) : Enum {

        FAILED("FLD"),
        PASSED("PAS"),
        IN_PROGRESS("INP"),
        NOT_STARTED("NST"),
        ERROR("ERR");

        companion object {

            @Converter(autoApply = true)
            class SolutionStatusConverter : AbstractEnumConverter<SolutionStatus>()
        }
    }

    /**
     * Solution type enum class.
     * @since 2.0.0
     */
    enum class SolutionType(override val dbkey: String) : Enum {

        QRS("QRS"),
        PYTHON("PY"),
        JAVASCRIPT("JS");

        companion object {

            @Converter(autoApply = true)
            class SolutionTypeConverter : AbstractEnumConverter<SolutionType>()
        }
    }

    companion object {

        fun qrsSolution(task: Task) = Solution(SolutionType.QRS).also {
            it.task = task
        }
    }
}