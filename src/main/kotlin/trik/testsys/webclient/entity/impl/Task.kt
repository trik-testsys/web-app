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

    @ManyToMany(mappedBy = "tasks", cascade = [CascadeType.ALL])
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
    val polygonsCount: Long
        get() = polygons.size.toLong()

    @get:Transient
    val hasExercise: Boolean
        get() = exercise != null

    @get:Transient
    val hasSolution: Boolean
        get() = solution != null

    @Column(nullable = false, unique = false, updatable = true)
    var passedTests: Boolean = false
        private set

    fun fail() {
        passedTests = false
    }

    fun pass() {
        passedTests = true
    }
}