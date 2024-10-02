package trik.testsys.webclient.service

import org.springframework.web.multipart.MultipartFile
import trik.testsys.webclient.entity.impl.TaskFile
import java.io.File

/**
 * Interface for managing files.
 * @author Roman Shishkin
 * @since 2.0.0
 */
interface FileManager {

    fun saveTaskFile(taskFile: TaskFile, fileData: MultipartFile): Boolean

    fun getTaskFile(taskFile: TaskFile): File?

    fun getTaskFiles(taskNameId: Long): Collection<TaskFile>

    fun getSolutionFile(solutionId: Long): File?
}