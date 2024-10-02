package trik.testsys.webclient.service

import org.springframework.web.multipart.MultipartFile
import trik.testsys.webclient.entity.impl.Solution
import trik.testsys.webclient.entity.impl.Task
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

    fun saveSolutionFile(solution: Solution, file: File): Boolean

    fun getTaskFiles(task: Task): Collection<TaskFile>

    fun getSolutionFile(solution: Solution): File?
}