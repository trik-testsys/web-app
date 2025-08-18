//package trik.testsys.webapp.backoffice.data.entity.impl
//
//import jakarta.persistence.Column
//import jakarta.persistence.Converter
//import jakarta.persistence.Entity
//import jakarta.persistence.Table
//import trik.testsys.core.data.entity.AbstractEntity.Companion.TABLE_PREFIX
//import trik.testsys.sac.data.entity.audit.AbstractAuditableEntity
//import trik.testsys.core.utils.enums.PersistableEnum
//import trik.testsys.core.utils.enums.converter.AbstractPersistableEnumConverter
//import trik.testsys.backoffice.entity.User
//import jakarta.persistence.*
//
//@Entity
//@Table(name = "${TABLE_PREFIX}solution")
//class Solution(
//    @Column(nullable = false, unique = false, updatable = false)
//    val type: SolutionType,
//) : AbstractAuditableEntity() {
//
//    @ManyToOne
//    @JoinColumn(
//        nullable = false, unique = false, updatable = false,
//        name = "task_id", referencedColumnName = "id"
//    )
//    lateinit var task: Task
//
//    @ManyToOne
//    @JoinColumn(
//        nullable = true, unique = false, updatable = false,
//        name = "student_id", referencedColumnName = "id"
//    )
//    var student: User? = null
//
//    @get:Transient
//    val isTest: Boolean
//        get() = student == null
//
//    @Column(nullable = false, unique = false, updatable = true)
//    var status: SolutionStatus = SolutionStatus.NOT_STARTED
//
//    @Column(nullable = false, unique = false, updatable = true)
//    var score: Long = -1
//
//    fun isScored() = score != -1L
//
//    // solutions should be linked not only with tasks but also with contests
//
//    /**
//     * @author Roman Shishkin
//     * @since 1.1.0
//     */
//    enum class SolutionStatus(override val dbkey: String) : PersistableEnum {
//
//        NOT_STARTED("NST"),
//        IN_PROGRESS("INP"),
//        FAILED("FLD"),
//        PASSED("PAS"),
//        ERROR("ERR");
//
//        companion object {
//
//            @Converter(autoApply = true)
//            class SolutionStatusConverter : AbstractPersistableEnumConverter<SolutionStatus>()
//        }
//    }
//
//    /**
//     * Solution type enum class.
//     * @since 2.0.0
//     */
//    enum class SolutionType(override val dbkey: String) : PersistableEnum {
//
//        QRS("QRS"),
//        PYTHON("PY"),
//        JAVASCRIPT("JS");
//
//        companion object {
//
//            @Converter(autoApply = true)
//            class SolutionTypeConverter : AbstractPersistableEnumConverter<SolutionType>()
//        }
//    }
//
//    companion object {
//
//        fun qrsSolution(task: Task) = Solution(SolutionType.QRS).also {
//            it.task = task
//        }
//    }
//}