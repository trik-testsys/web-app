//package trik.testsys.webapp.backoffice.data.entity.impl
//
//import jakarta.persistence.Converter
//import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
//import trik.testsys.core.utils.enums.PersistableEnum
//import trik.testsys.core.utils.enums.converter.AbstractPersistableEnumConverter
//import trik.testsys.backoffice.entity.AbstractNotedEntity
//import javax.persistence.*
//
//@Entity
//@Table(name = "${TABLE_PREFIX}_TASK_FILE")
//class TaskFileDep(
//    name: String,
//
//    @Column(nullable = false, unique = false, updatable = false)
//    val type: TaskFileType
//) : AbstractNotedEntity(name) {
//
//    @ManyToOne
//    @JoinColumn(
//        nullable = false, unique = false, updatable = false,
//        name = "developer_id", referencedColumnName = "id"
//    )
//    lateinit var developer: User
//
//    @ManyToMany(fetch = FetchType.EAGER)
//    @JoinTable(
//        name = "TASK_FILES_BY_TASKS",
//        joinColumns = [JoinColumn(name = "task_file_id")],
//        inverseJoinColumns = [JoinColumn(name = "task_id")]
//    )
//    val tasks: MutableSet<Task> = mutableSetOf()
//
//    @OneToMany(mappedBy = "taskFile", fetch = FetchType.EAGER)
//    val taskFileAudits: MutableSet<TaskFileAuditDep> = mutableSetOf()
//
//    @get:Transient
//    val latestAudit: TaskFileAuditDep
//        get() = taskFileAudits.maxBy { it.creationDate!! }
//
//    @get:Transient
//    val latestFileName: String
//        get() = latestAudit.fileName
//
//    enum class TaskFileType(override val dbkey: String) : PersistableEnum {
//
//        POLYGON("PLG"),
//        EXERCISE("EXR"),
//        SOLUTION("SLN"),
//        CONDITION("CND");
//
//        fun canBeRemovedOnTaskTesting() = this == CONDITION || this == EXERCISE
//
//        fun cannotBeRemovedOnTaskTesting() = !canBeRemovedOnTaskTesting()
//
//        companion object {
//
//            @Converter(autoApply = true)
//            class TaskFileTypeConverter : AbstractPersistableEnumConverter<TaskFileType>()
//
//            fun TaskFileType.localized() = when(this) {
//                POLYGON -> "Полигон"
//                EXERCISE -> "Упражнение"
//                SOLUTION -> "Решение"
//                CONDITION -> "Условие"
//            }
//        }
//    }
//
//    companion object {
//
//        fun polygon(name: String) = TaskFileDep(name, TaskFileType.POLYGON)
//
//        fun exercise(name: String) = TaskFileDep(name, TaskFileType.EXERCISE)
//
//        fun solution(name: String) = TaskFileDep(name, TaskFileType.SOLUTION)
//
//        fun condition(name: String) = TaskFileDep(name, TaskFileType.CONDITION)
//    }
//}