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
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import trik.testsys.webapp.core.data.entity.AbstractEntity
import trik.testsys.webapp.core.data.entity.AbstractEntity.Companion.TABLE_PREFIX
import trik.testsys.webapp.core.utils.enums.PersistableEnum
import trik.testsys.webapp.core.utils.enums.converter.AbstractPersistableEnumConverter
import trik.testsys.webapp.backoffice.data.entity.Sharable


@Entity
@Table(name = "${TABLE_PREFIX}task")
class Task() : AbstractEntity(), Sharable {

    @Column(name = "name")
    var name: String? = null

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "developer_id", nullable = false)
    var developer: User? = null

    @Deprecated("")
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "${TABLE_PREFIX}task_taskFiles",
        joinColumns = [JoinColumn(name = "task_id")],
        inverseJoinColumns = [JoinColumn(name = "taskFiles_id")],
    )
    var taskFiles: MutableSet<TaskFile> = mutableSetOf()

    @OneToMany(mappedBy = "task", orphanRemoval = true, fetch = FetchType.EAGER)
    var solutions: MutableSet<Solution> = mutableSetOf()

    @get:Transient
    val tests: Set<Solution>
        get() = solutions.filter { it.isTest }.toSet()

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "${TABLE_PREFIX}task_userGroups",
        joinColumns = [JoinColumn(name = "task_id")],
        inverseJoinColumns = [JoinColumn(name = "userGroups_id")]
    )
    override var userGroups: MutableSet<UserGroup> = mutableSetOf()

    @Enumerated(EnumType.STRING)
    @Column(name = "testing_status", nullable = false)
    var testingStatus: TestingStatus = TestingStatus.NOT_TESTED

    @Deprecated("")
    @get:Transient
    val polygonTaskFiles: Set<TaskFile>
        get() = taskFiles.filter { it.type == TaskFile.TaskFileType.POLYGON }.toSet()

    @Column(name = "data", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    var data: Data = Data()

    @get:Transient
    val availableSolutionTypes: Set<Solution.SolutionType>
        get() = data.solutionFileDataById.values.map { it.type }.toSet()

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

    data class Data(
        val conditionFileIds: MutableList<Long> = mutableListOf(),
        val exerciseFileIds: MutableList<Long> = mutableListOf(),
        val polygonFileIds: MutableList<Long> = mutableListOf(),
        val solutionFileDataById: MutableMap<Long, SolutionFileData> = mutableMapOf(),
    )

    data class SolutionFileData(
        val type: Solution.SolutionType = Solution.SolutionType.QRS,
        var lastSolutionId: Long? = null,
        var score: Long = 0,
        var lastTestScore: Long? = null
    )
}