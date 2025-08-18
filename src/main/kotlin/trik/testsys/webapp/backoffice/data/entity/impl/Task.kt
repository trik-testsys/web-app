//package trik.testsys.webapp.backoffice.data.entity.impl
//
//import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
//import trik.testsys.backoffice.entity.AbstractNotedEntity
//import javax.persistence.*
//
//@Entity
//@Table(name = "${TABLE_PREFIX}_TASK")
//class Task(
//    name: String
//) : AbstractNotedEntity(name) {
//
//    /**
//     * @author Roman Shishkin
//     * @since 1.1.0
//     */
//    @ManyToOne
//    @JoinColumn(
//        nullable = false, unique = false, updatable = false,
//        name = "developer_id", referencedColumnName = "id"
//    )
//    lateinit var developer: User
//
//    @ManyToMany(mappedBy = "tasks", fetch = FetchType.EAGER)
//    val taskFiles: MutableSet<TaskFileDep> = mutableSetOf()
//
//    @get:Transient
//    val polygons: Set<TaskFileDep>
//        get() = taskFiles.filter { it.type == TaskFileDep.TaskFileType.POLYGON }.toSet()
//
//    @get:Transient
//    val exercise: TaskFileDep?
//        get() = taskFiles.firstOrNull { it.type == TaskFileDep.TaskFileType.EXERCISE }
//
//    @get:Transient
//    val solution: TaskFileDep?
//        get() = taskFiles.firstOrNull { it.type == TaskFileDep.TaskFileType.SOLUTION }
//
//    @get:Transient
//    val condition: TaskFileDep?
//        get() = taskFiles.firstOrNull { it.type == TaskFileDep.TaskFileType.CONDITION }
//
//    @get:Transient
//    val polygonsCount: Long
//        get() = polygons.size.toLong()
//
//    @get:Transient
//    val hasExercise: Boolean
//        get() = exercise != null
//
//    @get:Transient
//    val hasSolution: Boolean
//        get() = solution != null
//
//    @get:Transient
//    val hasCondition: Boolean
//        get() = condition != null
//
//    @Column(nullable = false, unique = false, updatable = true)
//    var passedTests: Boolean = false
//        private set
//
//    fun fail() {
//        passedTests = false
//    }
//
//    fun pass() {
//        passedTests = true
//    }
//
//    @get:Transient
//    val truncatedName: String
//        get() = name.removePostfix()
//
//    fun compareNames(other: Task): Boolean {
//        return truncatedName == other.truncatedName
//    }
//
//    @ManyToMany(fetch = FetchType.EAGER)
//    @JoinTable(
//        name = "TASKS_BY_CONTESTS",
//        joinColumns = [JoinColumn(name = "task_id")],
//        inverseJoinColumns = [JoinColumn(name = "contest_id")]
//    )
//    val contests: MutableSet<Contest> = mutableSetOf()
//
//    companion object {
//
//        private const val TRIK_POSTFIX1 = "TRIK"
//        private const val TRIK_POSTFIX2 = "ТРИК"
//
//        private const val EV3_POSTFIX = "EV3"
//
//        private val POSTFIXES = setOf(TRIK_POSTFIX1, TRIK_POSTFIX2, EV3_POSTFIX)
//
//        private fun String.removePostfix(): String {
//            return POSTFIXES.fold(this) { acc, postfix -> acc.removeSuffix(postfix) }
//        }
//    }
//}