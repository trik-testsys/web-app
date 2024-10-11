package trik.testsys.webclient.entity.impl

import trik.testsys.core.entity.AbstractEntity
import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.utils.enums.Enum
import trik.testsys.core.utils.enums.converter.AbstractEnumConverter
import trik.testsys.webclient.entity.user.impl.Developer
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

    /**
     * @author Roman Shishkin
     * @since 2.0.0
     **/
    @ManyToOne
    @JoinColumn(
        nullable = false, unique = false, updatable = false,
        name = "developer_id", referencedColumnName = "id"
    )
    lateinit var developer: Developer

    @Column(nullable = false, unique = false, updatable = true)
    var status: SolutionStatus = SolutionStatus.NOT_STARTED

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    enum class SolutionStatus(override val dbkey: String) : Enum {

        FAILED("FLD"),
        PASSED("PAS"),
        IN_PROGRESS("INP"),
        NOT_STARTED("NST"),
        ERROR("ERR"),
        PARTIAL("PAR");

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
}