package trik.testsys.webapp.backoffice.data.entity.impl

import jakarta.persistence.Column
import jakarta.persistence.Converter
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.Transient
import trik.testsys.webapp.core.data.entity.AbstractEntity
import trik.testsys.webapp.core.data.entity.AbstractEntity.Companion.TABLE_PREFIX
import trik.testsys.webapp.core.utils.enums.PersistableEnum
import trik.testsys.webapp.core.utils.enums.converter.AbstractPersistableEnumConverter


@Entity
@Table(name = "${TABLE_PREFIX}task")
class Task() : AbstractEntity() {

    @Column(name = "name")
    var name: String? = null

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "developer_id", nullable = false)
    var developer: User? = null

//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "created_from_id", nullable = false)
//    var createdFrom: TaskTemplate? = null

    @ManyToMany
    @JoinTable(
        name = "${TABLE_PREFIX}task_taskFiles",
        joinColumns = [JoinColumn(name = "task_id")],
        inverseJoinColumns = [JoinColumn(name = "taskFiles_id")]
    )
    var taskFiles: MutableSet<TaskFile> = mutableSetOf()

    @OneToMany(mappedBy = "task", orphanRemoval = true)
    var solutions: MutableSet<Solution> = mutableSetOf()

    @get:Transient
    val tests: Set<Solution>
        get() = solutions.filter { it.isTest }.toSet()

    @Enumerated(EnumType.STRING)
    @Column(name = "testing_status", nullable = false)
    var testingStatus: TestingStatus = TestingStatus.NOT_TESTED

    enum class TestingStatus(override val dbKey: String) : PersistableEnum {

        NOT_TESTED("NTR"),
        TESTING("TST"),
        PASSED("PSD"),
        FAILED("FLD");

        companion object {

            @Converter(autoApply = true)
            class EnumConverter : AbstractPersistableEnumConverter<TestingStatus>()
        }
    }
}