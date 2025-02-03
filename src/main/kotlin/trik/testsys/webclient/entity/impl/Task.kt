package trik.testsys.webclient.entity.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.webclient.entity.AbstractNotedEntity
import trik.testsys.webclient.entity.user.impl.Developer
import javax.persistence.*

@Entity
@Table(name = "${TABLE_PREFIX}_TASK")
class Task(
    name: String
) : AbstractNotedEntity(name) {

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    @ManyToOne
    @JoinColumn(
        nullable = false, unique = false, updatable = false,
        name = "developer_id", referencedColumnName = "id"
    )
    lateinit var developer: Developer

    @ManyToMany(mappedBy = "tasks", fetch = FetchType.EAGER)
    val taskFiles: MutableSet<TaskFile> = mutableSetOf()

    @get:Transient
    val polygons: Set<TaskFile>
        get() = taskFiles.filter { it.type == TaskFile.TaskFileType.POLYGON }.toSet()

    @get:Transient
    val exercise: TaskFile?
        get() = taskFiles.firstOrNull { it.type == TaskFile.TaskFileType.EXERCISE }

    @get:Transient
    val solution: TaskFile?
        get() = taskFiles.firstOrNull { it.type == TaskFile.TaskFileType.SOLUTION }

    @get:Transient
    val condition: TaskFile?
        get() = taskFiles.firstOrNull { it.type == TaskFile.TaskFileType.CONDITION }

    @get:Transient
    val polygonsCount: Long
        get() = polygons.size.toLong()

    @get:Transient
    val hasExercise: Boolean
        get() = exercise != null

    @get:Transient
    val hasSolution: Boolean
        get() = solution != null

    @get:Transient
    val hasCondition: Boolean
        get() = condition != null

    @Column(nullable = false, unique = false, updatable = true)
    var passedTests: Boolean = false
        private set

    fun fail() {
        passedTests = false
    }

    fun pass() {
        passedTests = true
    }

    @get:Transient
    val truncatedName: String
        get() = name.removePostfix()

    fun compareNames(other: Task): Boolean {
        return truncatedName == other.truncatedName
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "TASKS_BY_CONTESTS",
        joinColumns = [JoinColumn(name = "task_id")],
        inverseJoinColumns = [JoinColumn(name = "contest_id")]
    )
    val contests: MutableSet<Contest> = mutableSetOf()

    companion object {

        private const val TRIK_POSTFIX1 = "TRIK"
        private const val TRIK_POSTFIX2 = "ТРИК"

        private const val EV3_POSTFIX = "EV3"

        private val POSTFIXES = setOf(TRIK_POSTFIX1, TRIK_POSTFIX2, EV3_POSTFIX)

        private fun String.removePostfix(): String {
            return POSTFIXES.fold(this) { acc, postfix -> acc.removeSuffix(postfix) }
        }
    }
}