package trik.testsys.webclient.service.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.zeroturnaround.zip.ZipUtil
import trik.testsys.webclient.entity.impl.Solution
import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.entity.impl.TaskFile
import trik.testsys.webclient.service.FileManager
import trik.testsys.webclient.service.Grader
import java.io.*
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
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
    @Value("\${path.taskFiles.conditions}") taskFileConditionsPath: String,


    @Value("\${path.files.solutions}") solutionsPath: String,
    @Value("\${path.files.verdicts}") verdictsPath: String,
    @Value("\${path.files.recordings}") recordingsPath: String,
    @Value("\${path.files.results}") resultsPath: String
) : FileManager {

    private val taskFileSolutionsDir = File(taskFileSolutionsPath)
    private val taskFileExercisesDir = File(taskFileExercisesPath)
    private val taskFilePolygonsDir = File(taskFilePolygonsPath)
    private val taskFileConditionsDir = File(taskFileConditionsPath)

    private val solutionsDir = File(solutionsPath)
    private val verdictsDir = File(verdictsPath)
    private val recordingsDir = File(recordingsPath)
    private val resultsDir = File(resultsPath)

    @PostConstruct
    fun init() {
        if (!taskFileSolutionsDir.exists()) taskFileSolutionsDir.mkdirs()
        if (!taskFileExercisesDir.exists()) taskFileExercisesDir.mkdirs()
        if (!taskFilePolygonsDir.exists()) taskFilePolygonsDir.mkdirs()
        if (!taskFileConditionsDir.exists()) taskFileConditionsDir.mkdirs()

        if (!solutionsDir.exists()) solutionsDir.mkdirs()
        if (!verdictsDir.exists()) verdictsDir.mkdirs()
        if (!recordingsDir.exists()) recordingsDir.mkdirs()
        if (!resultsDir.exists()) resultsDir.mkdirs()
    }

    override fun saveTaskFile(taskFile: TaskFile, fileData: MultipartFile): Boolean {
        logger.info("Saving task file with id ${taskFile.id}")

        val dir = getTaskFileDir(taskFile)
        val extension = getTaskFileExtension(taskFile)

        try {
            val file = File(dir, "${taskFile.id}$extension")
            fileData.transferTo(file)
        } catch (e: Exception) {
            logger.error("Error while saving task file with id ${taskFile.id}", e)
            return false
        }

        return true
    }

    override fun getTaskFile(taskFile: TaskFile): File? {
        logger.info("Getting task file with type '${taskFile.type}' and id ${taskFile.id}")

        val dir = getTaskFileDir(taskFile)
        val extension = getTaskFileExtension(taskFile)
        val file = File(dir, "${taskFile.id}$extension")

        if (!file.exists()) {
            logger.error("Task file with id ${taskFile.id} not found")
            return null
        }

        return file
    }

    private fun getTaskFileExtension(taskFile: TaskFile) = when (taskFile.type) {
        TaskFile.TaskFileType.SOLUTION -> ".qrs"
        TaskFile.TaskFileType.EXERCISE -> ".qrs"
        TaskFile.TaskFileType.POLYGON -> ".xml"
        TaskFile.TaskFileType.CONDITION -> ".pdf"
    }

    private fun getTaskFileDir(taskFile: TaskFile) = when (taskFile.type) {
        TaskFile.TaskFileType.SOLUTION -> taskFileSolutionsDir
        TaskFile.TaskFileType.EXERCISE -> taskFileExercisesDir
        TaskFile.TaskFileType.POLYGON -> taskFilePolygonsDir
        TaskFile.TaskFileType.CONDITION -> taskFileConditionsDir
    }

    override fun getTaskFiles(task: Task): Collection<TaskFile> {
        TODO()
    }

    override fun saveSolutionFile(solution: Solution, file: File): Boolean {
        logger.info("Saving solution file with id ${solution.id}")

        val solutionFile = File(solutionsDir, "${solution.id}.qrs")

        if (!file.exists())  {
            logger.error("Solution file with id ${solution.id} not found")
            return false
        }

        try {
            Files.copy(file.toPath(), solutionFile.toPath())
        } catch (e: Exception) {
            logger.error("Error while saving solution file with id ${solution.id}", e)
            return false
        }

        return true
    }

    override fun saveSolutionFile(solution: Solution, fileData: MultipartFile): Boolean {
        logger.info("Saving solution file with id ${solution.id}")

        try {
            val solutionFile = File(solutionsDir, "${solution.id}.qrs")
            fileData.transferTo(solutionFile)
        } catch (e: Exception) {
            logger.error("Error while saving solution file with id ${solution.id}", e)
            return false
        }

        return true
    }

    override fun getSolutionFile(solution: Solution): File? {
        logger.info("Getting solution file with id ${solution.id}")

        val file = File(solutionsDir, "${solution.id}.qrs")

        if (!file.exists()) {
            logger.error("Solution file with id ${solution.id} not found")
            return null
        }

        return file
    }

    override fun saveSuccessfulGradingInfo(fieldResult: Grader.GradingInfo.Ok) {
        logger.info("Saving ok grading info")

        val (solutionId, fieldResults) = fieldResult
        fieldResults.forEach { (fieldName, verdict, recording) ->
            logger.info("Field $fieldName: verdict ${verdict.name}, recording ${recording?.name}")

            verdict.content.let { verdictContent ->
                val verdictFile = File(verdictsDir, "${solutionId}_$fieldName.txt")
                verdictFile.writeBytes(verdictContent)

                logger.info("Verdict saved to ${verdictFile.absolutePath}")
            }

            recording?.content?.let { recordingContent ->
                val recordingFile = File(recordingsDir, "${solutionId}_$fieldName.mp4")
                recordingFile.writeBytes(recordingContent)

                logger.info("Recording saved to ${recordingFile.absolutePath}")
            }

        }
    }

    override fun getVerdictFiles(solution: Solution): List<File> {
        logger.info("Getting verdict files for solution with id ${solution.id}")

        val verdictFiles = verdictsDir.listFiles { _, name -> name.startsWith("${solution.id}_") } ?: emptyArray()

        return verdictFiles.toList()
    }

    override fun getRecordingFiles(solution: Solution): List<File> {
        logger.info("Getting recording files for solution with id ${solution.id}")

        val recordingFiles = recordingsDir.listFiles { _, name -> name.startsWith("${solution.id}_") } ?: emptyArray()

        return recordingFiles.toList()
    }

    override fun getSolutionResultFilesCompressed(solution: Solution): File {
        logger.info("Getting compressed solution result files for solution with id ${solution.id}")

        val resultsFile = File(resultsDir, "${solution.id}_results.zip")

        if (resultsFile.exists()) {
            logger.info("Compressed solution result files for solution with id ${solution.id} already exist")

            return resultsFile
        }

        val verdicts = getVerdictFiles(solution)
        val recordings = getRecordingFiles(solution)
        val results = verdicts + recordings

        ZipUtil.packEntries(results.toTypedArray(), resultsFile)

        return resultsFile
    }

    companion object {

        private val logger = LoggerFactory.getLogger(FileManagerImpl::class.java)
    }
}