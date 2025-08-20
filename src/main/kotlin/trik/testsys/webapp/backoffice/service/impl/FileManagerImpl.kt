package trik.testsys.webapp.backoffice.service.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import trik.testsys.webapp.backoffice.data.entity.impl.TaskFile
import trik.testsys.webapp.backoffice.data.entity.impl.TaskFile.TaskFileType.Companion.extension
import trik.testsys.webapp.backoffice.data.service.TaskFileService
import trik.testsys.webapp.backoffice.service.FileManager
import trik.testsys.webapp.backoffice.service.TaskFileVersionInfo
import java.io.File
import java.time.Instant
import javax.annotation.PostConstruct

@Service
class FileManagerImpl(
    @Value("\${trik.testsys.paths.taskFiles.solutions}") private val solutionsDirPath: String,
    @Value("\${trik.testsys.paths.taskFiles.exercises}") private val exercisesDirPath: String,
    @Value("\${trik.testsys.paths.taskFiles.polygons}") private val polygonsDirPath: String,
    @Value("\${trik.testsys.paths.taskFiles.conditions}") private val conditionsDirPath: String,

    private val taskFileService: TaskFileService
) : FileManager {

    private val solutionsDir = File(solutionsDirPath)
    private val exercisesDir = File(exercisesDirPath)
    private val polygonsDir = File(polygonsDirPath)
    private val conditionsDir = File(conditionsDirPath)

    private val dirByTaskFileType: Map<TaskFile.TaskFileType, File> by lazy {
        mapOf(
            TaskFile.TaskFileType.SOLUTION to solutionsDir,
            TaskFile.TaskFileType.EXERCISE to exercisesDir,
            TaskFile.TaskFileType.POLYGON to polygonsDir,
            TaskFile.TaskFileType.CONDITION to conditionsDir,
        )
    }

    @PostConstruct
    fun init() {
        listOf(solutionsDir, exercisesDir, polygonsDir, conditionsDir).forEach { dir ->
            if (!dir.exists()) dir.mkdirs()
        }
    }

    override fun saveTaskFile(taskFile: TaskFile, fileData: MultipartFile): Boolean {
        val saved = taskFileService.save(taskFile)

        val dir = dirByTaskFileType[saved.type] ?: error("UNDEFINED")
        val file = File(dir, saved.fileName)

        return try {
            fileData.transferTo(file)
            true
        } catch (e: Exception) {
            logger.error("Failed to save task file(id=${taskFile.id})", e)
            false
        }
    }

    override fun getTaskFile(taskFile: TaskFile): File? {
        val dir = dirByTaskFileType[taskFile.type] ?: error("UNDEFINED")
        val file = File(dir, taskFile.fileName)

        return if (file.exists()) file else null
    }

    override fun listTaskFileVersions(taskFile: TaskFile): List<TaskFileVersionInfo> {
        val dir = dirByTaskFileType[taskFile.type] ?: return emptyList()
        val prefix = "${taskFile.id}-"
        val files = dir.listFiles { _, name -> name.startsWith(prefix) } ?: emptyArray()
        return files.mapNotNull { f ->
            val ver = f.name.removePrefix(prefix).substringBeforeLast('.')
            ver.toLongOrNull()?.let { v ->
                TaskFileVersionInfo(version = v, fileName = f.name, lastModifiedAt = Instant.ofEpochMilli(f.lastModified()))
            }
        }.sortedByDescending { it.version }
    }

    override fun getTaskFileVersion(taskFile: TaskFile, version: Long): File? {
        val dir = dirByTaskFileType[taskFile.type] ?: return null
        val ext = taskFile.type?.extension() ?: return null
        val file = File(dir, "${taskFile.id}-${version}$ext")
        return if (file.exists()) file else null
    }

    companion object {

        private val logger = LoggerFactory.getLogger(FileManagerImpl::class.java)
    }
}