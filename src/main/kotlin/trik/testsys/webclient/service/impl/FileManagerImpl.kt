package trik.testsys.webclient.service.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import trik.testsys.webclient.entity.impl.TaskFile
import trik.testsys.webclient.service.FileManager
import java.io.File
import javax.annotation.PostConstruct

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
@Service
class FileManagerImpl(
    @Value("\${path.taskFiles.solutions}") taskFileSolutionsPath: String,
    @Value("\${path.taskFiles.exercises}") taskFileExercisesPath: String,
    @Value("\${path.taskFiles.polygons}") taskFilePolygonsPath: String,

    @Value("\${path.files.solutions}") solutionsPath: String
) : FileManager {

    private val taskFileSolutionsDir = File(taskFileSolutionsPath)
    private val taskFileExercisesDir = File(taskFileExercisesPath)
    private val taskFilePolygonsDir = File(taskFilePolygonsPath)

    private val solutionsDir = File(solutionsPath)

    @PostConstruct
    fun init() {
        if (!taskFileSolutionsDir.exists()) taskFileSolutionsDir.mkdirs()
        if (!taskFileExercisesDir.exists()) taskFileExercisesDir.mkdirs()
        if (!taskFilePolygonsDir.exists()) taskFilePolygonsDir.mkdirs()

        if (!solutionsDir.exists()) solutionsDir.mkdirs()
    }

    override fun saveTaskFile(taskFile: TaskFile, fileData: MultipartFile): Boolean {
        logger.info("Saving task file with id ${taskFile.id}")

        val dir = getTaskFileDir(taskFile)

        val fileExtension = fileData.originalFilename?.substringAfterLast(".") ?: ""

        try {
            val file = File(dir, "${taskFile.id}.$fileExtension")
            fileData.transferTo(file)
        } catch (e: Exception) {
            logger.error("Error while saving task file with id ${taskFile.id}", e)
            return false
        }

        return true
    }

    override fun getTaskFile(taskFile: TaskFile): File? {
        logger.info("Getting task file with id ${taskFile.id}")

        val dir = getTaskFileDir(taskFile)
        val file = File(dir, "${taskFile.id}")

        if (!file.exists()) {
            logger.error("Task file with id ${taskFile.id} not found")
            return null
        }

        return file
    }

    private fun getTaskFileDir(taskFile: TaskFile) = when (taskFile.type) {
        TaskFile.TaskFileType.SOLUTION -> taskFileSolutionsDir
        TaskFile.TaskFileType.EXERCISE -> taskFileExercisesDir
        TaskFile.TaskFileType.POLYGON -> taskFilePolygonsDir
    }

    override fun getTaskFiles(taskNameId: Long): Collection<TaskFile> {
        TODO("Not yet implemented")
    }

    override fun getSolutionFile(solutionId: Long): File? {
        logger.info("Getting solution file with id $solutionId")

        val file = File(solutionsDir, "$solutionId")

        if (!file.exists()) {
            logger.error("Solution file with id $solutionId not found")
            return null
        }

        return file
    }

    companion object {

        private val logger = LoggerFactory.getLogger(FileManagerImpl::class.java)
    }
}