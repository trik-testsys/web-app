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
    @Value("\${path.files.solutions}") solutionsPath: String,
    @Value("\${path.files.exercises}") exercisesPath: String,
    @Value("\${path.files.polygons}") polygonsPath: String
): FileManager{

    private val solutionsDir = File(solutionsPath)
    private val exercisesDir = File(exercisesPath)
    private val polygonsDir = File(polygonsPath)

    @PostConstruct
    fun init() {
        if (!solutionsDir.exists()) solutionsDir.mkdirs()
        if (!exercisesDir.exists()) exercisesDir.mkdirs()
        if (!polygonsDir.exists()) polygonsDir.mkdirs()
    }

    override fun saveTaskFile(taskFile: TaskFile, fileData: MultipartFile): Boolean {
        logger.info("Saving task file with id ${taskFile.id}")

        val dir = when (taskFile.type) {
            TaskFile.TaskFileType.SOLUTION -> solutionsDir
            TaskFile.TaskFileType.EXERCISE -> exercisesDir
            TaskFile.TaskFileType.POLYGON -> polygonsDir
        }

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

        val file = File(exercisesDir, "${taskFile.id}")
        if (!file.exists()) {
            logger.error("Task file with id ${taskFile.id} not found")
            return null
        }

        return file
    }

    override fun getTaskFiles(taskNameId: Long): Collection<TaskFile> {
        TODO("Not yet implemented")
    }

    override fun getSolutionFile(solutionId: Long): File? {
        TODO("Not yet implemented")
    }

    companion object {

        private val logger = LoggerFactory.getLogger(FileManagerImpl::class.java)
    }
}