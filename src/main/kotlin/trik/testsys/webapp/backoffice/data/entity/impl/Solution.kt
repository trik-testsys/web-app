package trik.testsys.webapp.backoffice.data.entity.impl

import jakarta.persistence.Column
import jakarta.persistence.Converter
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.*
import trik.testsys.webapp.core.data.entity.AbstractEntity
import trik.testsys.webapp.core.data.entity.AbstractEntity.Companion.TABLE_PREFIX
import trik.testsys.webapp.core.utils.enums.PersistableEnum
import trik.testsys.webapp.core.utils.enums.converter.AbstractPersistableEnumConverter

@Entity
@Table(name = "${TABLE_PREFIX}solution")
class Solution() : AbstractEntity() {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    lateinit var createdBy: User

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: Status = Status.NOT_STARTED

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "contest_id", nullable = true)
    var contest: Contest? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    lateinit var task: Task

    @get:Transient
    val isTest: Boolean
        get() = contest == null

    @Column(name = "relevant_verdict_id", nullable = true)
    var relevantVerdictId: Long? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: SolutionType = SolutionType.QRS

    enum class Status(override val dbKey: String) : PersistableEnum {

        NOT_STARTED("NST"),
        IN_PROGRESS("INP"),
        TIMEOUT("TMO"),
        PASSED("PAS"),
        ERROR("ERR");

        companion object {

            @Converter(autoApply = true)
            class StatusConverter : AbstractPersistableEnumConverter<Status>() {
                override fun convertToEntityAttribute(dbData: String?): Status? {
                    // Map legacy FAILED (FLD) to PASSED
                    if (dbData == "FLD") return PASSED
                    return super.convertToEntityAttribute(dbData)
                }
            }
        }
    }

    enum class SolutionType(override val dbKey: String, val extension: String) : PersistableEnum {

        QRS("QRS", "qrs"),
        PYTHON("PY", "py"),
        JAVA_SCRIPT("JS", "js");

        companion object {

            @Converter(autoApply = true)
            class SolutionTypeConverter : AbstractPersistableEnumConverter<SolutionType>()
        }
    }

    @get:Transient
    val fileName: String
        get() = "solution-$id.$extension"

    @get:Transient
    val extension: String
        get() = type.extension
}