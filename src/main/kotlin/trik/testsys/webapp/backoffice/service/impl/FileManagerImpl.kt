package trik.testsys.webapp.backoffice.service.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.zeroturnaround.zip.ZipUtil
import trik.testsys.webapp.backoffice.data.entity.impl.TaskFile
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.entity.impl.TaskFile.TaskFileType.Companion.extension
import trik.testsys.webapp.backoffice.data.service.TaskFileService
import trik.testsys.webapp.backoffice.service.FileManager
import trik.testsys.webapp.backoffice.service.Grader
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

    @Value("\${trik.testsys.paths.files.solutions}") private val solutionFilesPath: String,
    @Value("\${trik.testsys.paths.files.verdicts}") private val verdictFilesPath: String,
    @Value("\${trik.testsys.paths.files.recordings}") private val recordingFilesPath: String,
    @Value("\${trik.testsys.paths.files.results}") private val resultFilesPath: String,

    private val taskFileService: TaskFileService
) : FileManager {

    private val solutionsDir = File(solutionsDirPath)
    private val exercisesDir = File(exercisesDirPath)
    private val polygonsDir = File(polygonsDirPath)
    private val conditionsDir = File(conditionsDirPath)

    private val solutionFilesDir = File(solutionFilesPath)
    private val verdictFilesDir = File(verdictFilesPath)
    private val recordingFilesDir = File(recordingFilesPath)
    private val resultFilesDir = File(resultFilesPath)

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
        listOf(
            solutionsDir, exercisesDir, polygonsDir, conditionsDir,
            solutionFilesDir, verdictFilesDir, recordingFilesDir, resultFilesDir
        ).forEach { dir ->
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
                TaskFileVersionInfo(
                    v, f.name, Instant.ofEpochMilli(f.lastModified()),
                    taskFile.data.originalFileNameByVersion[v] ?: f.name
                )
            }
        }.sortedByDescending { it.version }
    }

    override fun getTaskFileVersion(taskFile: TaskFile, version: Long): File? {
        val dir = dirByTaskFileType[taskFile.type] ?: return null
        val ext = taskFile.type?.extension() ?: return null
        val file = File(dir, "${taskFile.id}-${version}$ext")
        return if (file.exists()) file else null
    }

    override fun saveSolutionFile(solution: Solution, fileData: MultipartFile): Boolean {
        // Persist original uploaded solution alongside task files under solutionsDir
        val file = File(solutionsDir, "solution-${solution.id}.qrs")
        return try {
            fileData.transferTo(file)
            true
        } catch (e: Exception) {
            logger.error("Failed to save solution file(id=${solution.id})", e)
            false
        }
    }

    override fun saveSolutionFile(solution: Solution, sourceFile: File): Boolean {
        val target = File(solutionsDir, "solution-${solution.id}.qrs")
        return try {
            sourceFile.copyTo(target, overwrite = true)
            true
        } catch (e: Exception) {
            logger.error("Failed to copy solution file(id=${solution.id}) from ${sourceFile.absolutePath}", e)
            false
        }
    }

    override fun getSolutionFile(solution: Solution): File? {
        val file = File(solutionsDir, "solution-${solution.id}.qrs")

        return if (file.exists()) file else null
    }

    override fun saveSuccessfulGradingInfo(fieldResult: Grader.GradingInfo.Ok) {
        logger.info("Saving ok grading info")

        val (solutionId, fieldResults) = fieldResult
        fieldResults.forEach { (fieldName, verdict, recording) ->
            logger.info("Field $fieldName: verdict ${verdict.name}, recording ${recording?.name}")

            verdict.content.let { verdictContent ->
                val verdictFile = File(verdictFilesDir, "${solutionId}_$fieldName.txt")
                verdictFile.writeBytes(verdictContent)

                logger.info("Verdict saved to ${verdictFile.absolutePath}")
            }

            recording?.content?.let { recordingContent ->
                val recordingFile = File(recordingFilesDir, "${solutionId}_$fieldName.mp4")
                recordingFile.writeBytes(recordingContent)

                logger.info("Recording saved to ${recordingFile.absolutePath}")
            }

        }
    }

    override fun getVerdictFiles(solution: Solution): List<File> {
        logger.info("Getting verdict files for solution with id ${solution.id}")

        val verdictFiles = verdictFilesDir.listFiles { _, name -> name.startsWith("${solution.id}_") } ?: emptyArray()

        return verdictFiles.toList()
    }

    override fun getRecordingFiles(solution: Solution): List<File> {
        logger.info("Getting recording files for solution with id ${solution.id}")

        val recordingFiles = recordingFilesDir.listFiles { _, name -> name.startsWith("${solution.id}_") } ?: emptyArray()

        return recordingFiles.toList()
    }

    override fun getSolutionResultFilesCompressed(solution: Solution): File {
        logger.info("Getting compressed solution result files for solution with id ${solution.id}")

        val resultsFile = File(resultFilesDir, "${solution.id}_results.zip")

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