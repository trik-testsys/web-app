package trik.testsys.webclient.entity.impl

import trik.testsys.core.entity.AbstractEntity
import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.utils.enums.Enum
import trik.testsys.core.utils.enums.converter.AbstractEnumConverter
import trik.testsys.webclient.entity.user.impl.Developer
import trik.testsys.webclient.entity.user.impl.Student
import javax.persistence.*

@Entity
@Table(name = "${TABLE_PREFIX}_SOLUTION")
class Solution : AbstractEntity() {

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

    @ManyToOne
    @JoinColumn(
        nullable = false, unique = false, updatable = false,
        name = "student_id", referencedColumnName = "id"
    )
    lateinit var student: Student

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
}