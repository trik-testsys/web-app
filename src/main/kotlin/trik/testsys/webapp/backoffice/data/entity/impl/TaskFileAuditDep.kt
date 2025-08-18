//package trik.testsys.webapp.backoffice.data.entity.impl
//
//import trik.testsys.core.entity.AbstractEntity
//import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
//import javax.persistence.Entity
//import javax.persistence.JoinColumn
//import javax.persistence.ManyToOne
//import javax.persistence.Table
//
///**
// * @author Roman Shishkin
// * @since 2.1.0
// */
//@Entity
//@Table(name = "${TABLE_PREFIX}_TASK_FILE_AUDIT")
//class TaskFileAuditDep(
//    @ManyToOne
//    @JoinColumn(
//        nullable = false, unique = false, updatable = false,
//        name = "task_file_id", referencedColumnName = "id"
//    )
//    val taskFile: TaskFileDep
//) : AbstractEntity() {
//
//    @get:Transient
//    val fileName: String
//        get() = "${taskFile.id}-${id}"
//}