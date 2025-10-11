package trik.testsys.webapp.backoffice.service.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.zeroturnaround.zip.ZipUtil
import trik.testsys.webapp.backoffice.data.entity.impl.TaskFile
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.entity.impl.TaskFile.TaskFileType.Companion.extension
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.ConditionFile
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.ExerciseFile
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.PolygonFile
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.SolutionFile
import trik.testsys.webapp.backoffice.data.service.TaskFileService
import trik.testsys.webapp.backoffice.data.service.impl.taskFile.ConditionFileService
import trik.testsys.webapp.backoffice.data.service.impl.taskFile.ExerciseFileService
import trik.testsys.webapp.backoffice.data.service.impl.taskFile.PolygonFileService
import trik.testsys.webapp.backoffice.data.service.impl.taskFile.SolutionFileService
import trik.testsys.webapp.backoffice.service.FileManager
import trik.testsys.webapp.backoffice.service.Grader
import trik.testsys.webapp.backoffice.service.TaskFileVersionInfo
import java.io.File
import java.nio.file.Files
import java.time.Instant
import javax.annotation.PostConstruct
import kotlin.streams.asSequence

@Service
class FileManagerImpl(
    @Value("\${trik.testsys.paths.taskFiles.conditions}") private val conditionFilesDirPath: String,
    @Value("\${trik.testsys.paths.taskFiles.exercises}") private val exerciseFilesDirPath: String,
    @Value("\${trik.testsys.paths.taskFiles.polygons}") private val polygonFilesDirPath: String,
    @Value("\${trik.testsys.paths.taskFiles.solutions}") private val solutionFilesDirPath: String,

    @Value("\${trik.testsys.paths.files.solutions}") private val solutionsPath: String,
    @Value("\${trik.testsys.paths.files.verdicts}") private val verdictsPath: String,
    @Value("\${trik.testsys.paths.files.recordings}") private val recordingsPath: String,
    @Value("\${trik.testsys.paths.files.results}") private val resultsPath: String,

    private val taskFileService: TaskFileService,
    private val conditionFileService: ConditionFileService,
    private val exerciseFileService: ExerciseFileService,
    private val polygonFileService: PolygonFileService,
    private val solutionFileService: SolutionFileService
) : FileManager {

    private val solutionFilesDir = File(solutionFilesDirPath)
    private val exerciseFilesDir = File(exerciseFilesDirPath)
    private val polygonFilesDir = File(polygonFilesDirPath)
    private val conditionFilesDir = File(conditionFilesDirPath)

    private val solutionsDir = File(solutionsPath)
    private val verdictsDir = File(verdictsPath)
    private val recordingsDir = File(recordingsPath)
    private val resultsDir = File(resultsPath)

    @Deprecated("")
    private val dirByTaskFileType: Map<TaskFile.TaskFileType, File> by lazy {
        mapOf(
            TaskFile.TaskFileType.SOLUTION to solutionFilesDir,
            TaskFile.TaskFileType.EXERCISE to exerciseFilesDir,
            TaskFile.TaskFileType.POLYGON to polygonFilesDir,
            TaskFile.TaskFileType.CONDITION to conditionFilesDir,
        )
    }

    @PostConstruct
    fun init() {
        listOf(
            solutionFilesDir, exerciseFilesDir, polygonFilesDir, conditionFilesDir,
            solutionsDir, verdictsDir, recordingsDir, resultsDir
        ).forEach { dir ->
            if (!dir.exists()) dir.mkdirs()
        }
    }

    @Deprecated("")
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

    @Deprecated("")
    override fun getTaskFile(taskFile: TaskFile): File? {
        val dir = dirByTaskFileType[taskFile.type] ?: error("UNDEFINED")
        val file = File(dir, taskFile.fileName)

        return if (file.exists()) file else null
    }

    @Deprecated("")
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

    @Deprecated("")
    override fun getTaskFileVersion(taskFile: TaskFile, version: Long): File? {
        val dir = dirByTaskFileType[taskFile.type] ?: return null
        val ext = taskFile.type?.extension() ?: return null
        val file = File(dir, "${taskFile.id}-${version}$ext")
        return if (file.exists()) file else null
    }

    override fun saveSolution(solution: Solution, fileData: MultipartFile): Boolean {
        // Persist original uploaded solution alongside task files under solutionsDir
        val file = File(solution.dir, solution.fileName)
        return try {
            fileData.transferTo(file)
            true
        } catch (e: Exception) {
            logger.error("Failed to save solution file(id=${solution.id})", e)
            false
        }
    }

    override fun saveSolution(solution: Solution, sourceFile: File): Boolean {
        val target = File(solution.dir, solution.fileName)
        return try {
            sourceFile.copyTo(target, overwrite = true)
            true
        } catch (e: Exception) {
            logger.error("Failed to copy solution file(id=${solution.id}) from ${sourceFile.absolutePath}", e)
            false
        }
    }

    override fun getSolution(solution: Solution): File? {
        val file = File(solution.dir, solution.fileName)
        return if (file.exists()) file else null
    }

    override fun hasSolution(solution: Solution): Boolean {
        val has = Files.list(solution.dir.toPath()).use { stream ->
            stream.asSequence().firstOrNull { it.fileName.toString() == solution.fileName }
        }?.let { true } ?: false

        return has
    }

    // TODO: uncomment on major release
//    private val Solution.dir: File
//        get() = when (contest) {
//            null -> solutionsDir
//            else -> solutionFilesDir
//        }

    private val Solution.dir: File
        get() = solutionFilesDir

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

    override fun getVerdicts(solution: Solution): List<File> {
        logger.info("Getting verdict files for solution with id ${solution.id}")

        val verdictFiles = Files.list(verdictsDir.toPath()).use { stream ->
            stream.asSequence()
                .filter { it.fileName.toString().startsWith("${solution.id}_") }
                .map { it.toFile() }
                .toList()
        }

        return verdictFiles
    }

    override fun hasAnyVerdict(solution: Solution): Boolean {
        logger.debug("Finding verdict files for solution(id=${solution.id})")

        val hasAny = Files.list(verdictsDir.toPath()).use { stream ->
            stream.asSequence()
                .any { it.fileName.toString().startsWith("${solution.id}_") }
        }

        return hasAny
    }

    override fun getRecording(solution: Solution): List<File> {
        logger.info("Getting recording files for solution with id ${solution.id}")

        val recordingFiles = Files.list(recordingsDir.toPath()).use { stream ->
            stream.asSequence()
                .filter { it.fileName.toString().startsWith("${solution.id}_") }
                .map { it.toFile() }
                .toList()
        }

        return recordingFiles
    }

    override fun hasAnyRecording(solution: Solution): Boolean {
        logger.debug("Finding recording files for solution(id=${solution.id})")

        val hasAny = Files.list(recordingsDir.toPath()).use { stream ->
            stream.asSequence()
                .any { it.fileName.toString().startsWith("${solution.id}_") }
        }

        return hasAny
    }

    override fun getSolutionResultFilesCompressed(solution: Solution): File {
        logger.info("Getting compressed solution result files for solution with id ${solution.id}")

        val resultsFile = File(resultsDir, "${solution.id}_results.zip")

        if (resultsFile.exists()) {
            logger.info("Compressed solution result files for solution with id ${solution.id} already exist")

            return resultsFile
        }

        val verdicts = getVerdicts(solution)
        val recordings = getRecording(solution)
        val results = verdicts + recordings

        ZipUtil.packEntries(results.toTypedArray(), resultsFile)

        return resultsFile
    }

    override fun saveConditionFile(conditionFile: ConditionFile, fileData: MultipartFile): ConditionFile? {
        logger.debug("Saving conditionFile(id=${conditionFile.id}, type=${conditionFile.type})")

        val saved = conditionFileService.save(conditionFile)
        val file = File(conditionFilesDir, saved.getFileName())
        
        return try {
            fileData.transferTo(file)
            saved
        } catch (e: Exception) {
            logger.error("Failed to save condition file(id=${conditionFile.id})", e)
            conditionFileService.delete(saved)
            null
        }
    }

    override fun saveExerciseFile(exerciseFile: ExerciseFile, fileData: MultipartFile): ExerciseFile? {
        logger.debug("Saving exerciseFile(id=${exerciseFile.id}, type=${exerciseFile.type})")

        val saved = exerciseFileService.save(exerciseFile)
        val file = File(exerciseFilesDir, saved.getFileName())

        return try {
            fileData.transferTo(file)
            saved
        } catch (e: Exception) {
            logger.error("Failed to save exercise file(id=${exerciseFile.id})", e)
            exerciseFileService.delete(saved)
            null
        }
    }

    override fun savePolygonFile(polygonFile: PolygonFile, fileData: MultipartFile): PolygonFile? {
        logger.debug("Saving polygonFile(id=${polygonFile.id}, type=${polygonFile.type})")

        val saved = polygonFileService.save(polygonFile)
        val file = File(polygonFilesDir, saved.getFileName())

        return try {
            fileData.transferTo(file)
            saved
        } catch (e: Exception) {
            logger.error("Failed to save polygon file(id=${polygonFile.id})", e)
            polygonFileService.delete(saved)
            null
        }
    }

    override fun saveSolutionFile(solutionFile: SolutionFile, fileData: MultipartFile): SolutionFile? {
        logger.debug("Saving solutionFile(id=${solutionFile.id}, type=${solutionFile.type})")

        val saved = solutionFileService.save(solutionFile)
        val file = File(solutionFilesDir, saved.getFileName())

        return try {
            fileData.transferTo(file)
            saved
        } catch (e: Exception) {
            logger.error("Failed to save solution file(id=${solutionFile.id})", e)
            solutionFileService.delete(saved)
            null
        }
    }

    override fun saveConditionFile(conditionFile: ConditionFile, file: File): ConditionFile? {
        logger.debug("Saving conditionFile(id=${conditionFile.id}, type=${conditionFile.type}) from file ${file.absolutePath}")

        val saved = conditionFileService.save(conditionFile)
        val target = File(conditionFilesDir, saved.getFileName())

        return try {
            file.copyTo(target, overwrite = true)
            saved
        } catch (e: Exception) {
            logger.error("Failed to copy condition file(id=${conditionFile.id}) from ${file.absolutePath}", e)
            conditionFileService.delete(saved)
            null
        }
    }

    override fun saveExerciseFile(exerciseFile: ExerciseFile, file: File): ExerciseFile? {
        logger.debug("Saving exerciseFile(id=${exerciseFile.id}, type=${exerciseFile.type}) from file ${file.absolutePath}")

        val saved = exerciseFileService.save(exerciseFile)
        val target = File(exerciseFilesDir, saved.getFileName())

        return try {
            file.copyTo(target, overwrite = true)
            saved
        } catch (e: Exception) {
            logger.error("Failed to copy exercise file(id=${exerciseFile.id}) from ${file.absolutePath}", e)
            exerciseFileService.delete(saved)
            null
        }
    }

    override fun savePolygonFile(polygonFile: PolygonFile, file: File): PolygonFile? {
        logger.debug("Saving polygonFile(id=${polygonFile.id}, type=${polygonFile.type}) from file ${file.absolutePath}")

        val saved = polygonFileService.save(polygonFile)
        val target = File(polygonFilesDir, saved.getFileName())

        return try {
            file.copyTo(target, overwrite = true)
            saved
        } catch (e: Exception) {
            logger.error("Failed to copy polygon file(id=${polygonFile.id}) from ${file.absolutePath}", e)
            polygonFileService.delete(saved)
            null
        }
    }

    override fun saveSolutionFile(solutionFile: SolutionFile, file: File): SolutionFile? {
        logger.debug("Saving solutionFile(id=${solutionFile.id}, type=${solutionFile.type}) from file ${file.absolutePath}")

        val saved = solutionFileService.save(solutionFile)
        val target = File(solutionFilesDir, saved.getFileName())

        return try {
            file.copyTo(target, overwrite = true)
            saved
        } catch (e: Exception) {
            logger.error("Failed to copy solution file(id=${solutionFile.id}) from ${file.absolutePath}", e)
            solutionFileService.delete(saved)
            null
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(FileManagerImpl::class.java)
    }
}