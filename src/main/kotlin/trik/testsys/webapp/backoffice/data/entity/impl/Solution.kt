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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contest_id", nullable = false)
    var contest: Contest? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    lateinit var task: Task

    @get:Transient
    val isTest: Boolean
        get() = contest == null

    @OneToMany(mappedBy = "solution", orphanRemoval = true)
    var verdicts: MutableSet<Verdict> = mutableSetOf()

    @OneToOne(fetch = FetchType.EAGER, optional = true, orphanRemoval = true)
    @JoinColumn(name = "relevant_verdict_id", nullable = true, unique = true)
    var relevantVerdict: Verdict? = null

    enum class Status(override val dbKey: String) : PersistableEnum {

        NOT_STARTED("NST"),
        IN_PROGRESS("INP"),
        FAILED("FLD"),
        PASSED("PAS"),
        ERROR("ERR");

        companion object {

            @Converter(autoApply = true)
            class StatusConverter : AbstractPersistableEnumConverter<Status>()
        }
    }
}