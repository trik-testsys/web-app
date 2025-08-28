package trik.testsys.webapp.backoffice.service

import org.springframework.web.multipart.MultipartFile
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
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

    fun saveSolutionFile(solution: Solution, fileData: MultipartFile): Boolean

    fun saveSolutionFile(solution: Solution, sourceFile: File): Boolean

    fun getSolutionFile(solution: Solution): File?

    fun saveSuccessfulGradingInfo(fieldResult: Grader.GradingInfo.Ok)

    fun getVerdictFiles(solution: Solution): List<File>

    fun getRecordingFiles(solution: Solution): List<File>

    fun getSolutionResultFilesCompressed(solution: Solution): File
}

data class TaskFileVersionInfo(
    val version: Long,
    val fileName: String,
    val lastModifiedAt: Instant,
    val originalName: String
)