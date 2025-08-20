package trik.testsys.webapp.backoffice.service

import org.springframework.web.multipart.MultipartFile
import trik.testsys.webapp.backoffice.data.entity.impl.TaskFile
import java.io.File
import java.time.Instant

/**
 * File manager for handling TaskFile persistence on disk.
 */
interface FileManager {

    fun saveTaskFile(taskFile: TaskFile, fileData: MultipartFile): Boolean

    fun getTaskFile(taskFile: TaskFile): File?

    fun listTaskFileVersions(taskFile: TaskFile): List<TaskFileVersionInfo>

    fun getTaskFileVersion(taskFile: TaskFile, version: Long): File?
}

data class TaskFileVersionInfo(
    val version: Long,
    val fileName: String,
    val lastModifiedAt: Instant,
)

//package trik.testsys.webapp.backoffice.service
//
//import org.springframework.web.multipart.MultipartFile
//import trik.testsys.backoffice.entity.impl.Solution
//import trik.testsys.backoffice.entity.impl.Task
//import trik.testsys.backoffice.entity.impl.TaskFile
//import trik.testsys.backoffice.entity.impl.TaskFileAudit
//import java.io.File
//
///**
// * Interface for managing files.
// * @author Roman Shishkin
// * @since 2.0.0
// */
//interface FileManager {
//
//    fun saveTaskFile(taskFileAudit: TaskFileAudit, fileData: MultipartFile): Boolean
//
//    fun getTaskFile(taskFile: TaskFile): File?
//
//    fun getTaskFileAuditFile(taskFileAudit: TaskFileAudit): File?
//
//    fun getTaskFileExtension(taskFile: TaskFile): String
//
//    fun saveSolutionFile(solution: Solution, file: File): Boolean
//
//    fun saveSolutionFile(solution: Solution, fileData: MultipartFile): Boolean
//
//    fun getTaskFiles(task: Task): Collection<TaskFile>
//
//    fun getSolutionFile(solution: Solution): File?
//
//    fun saveSuccessfulGradingInfo(fieldResult: Grader.GradingInfo.Ok)
//
//    fun getVerdictFiles(solution: Solution): List<File>
//
//    fun getRecordingFiles(solution: Solution): List<File>
//
//    fun getSolutionResultFilesCompressed(solution: Solution): File
//}