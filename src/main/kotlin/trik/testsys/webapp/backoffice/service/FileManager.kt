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

    fun saveSolutionFile(solution: trik.testsys.webapp.backoffice.data.entity.impl.Solution, fileData: MultipartFile): Boolean
}

data class TaskFileVersionInfo(
    val version: Long,
    val fileName: String,
    val lastModifiedAt: Instant,
)