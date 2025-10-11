package trik.testsys.webapp.backoffice.service

import jakarta.transaction.Transactional
import org.springframework.web.multipart.MultipartFile
import trik.testsys.webapp.backoffice.data.entity.AbstractFile
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.entity.impl.TaskFile
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.ConditionFile
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.ExerciseFile
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.PolygonFile
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.SolutionFile
import java.io.File
import java.time.Instant

/**
 * File manager for handling TaskFile persistence on disk.
 */
interface FileManager {

    @Deprecated("Remove after migration")
    fun getTaskFileVersion(taskFile: TaskFile, version: Long): File?

    fun saveSolution(solution: Solution, fileData: MultipartFile): Boolean

    fun saveSolution(solution: Solution, sourceFile: File): Boolean

    fun getSolution(solution: Solution): File?

    fun hasSolution(solution: Solution): Boolean

    fun saveSuccessfulGradingInfo(fieldResult: Grader.GradingInfo.Ok)

    fun getVerdicts(solution: Solution): List<File>

    fun hasAnyVerdict(solution: Solution): Boolean

    fun getRecording(solution: Solution): List<File>

    fun hasAnyRecording(solution: Solution): Boolean

    fun getSolutionResultFilesCompressed(solution: Solution): File

    @Transactional
    fun saveConditionFile(conditionFile: ConditionFile, fileData: MultipartFile): ConditionFile?

    @Transactional
    fun saveExerciseFile(exerciseFile: ExerciseFile, fileData: MultipartFile): ExerciseFile?

    @Transactional
    fun savePolygonFile(polygonFile: PolygonFile, fileData: MultipartFile): PolygonFile?

    @Transactional
    fun saveSolutionFile(solutionFile: SolutionFile, fileData: MultipartFile): SolutionFile?

    @Transactional
    fun saveConditionFile(conditionFile: ConditionFile, file: File): ConditionFile?

    @Transactional
    fun saveExerciseFile(exerciseFile: ExerciseFile, file: File): ExerciseFile?

    @Transactional
    fun savePolygonFile(polygonFile: PolygonFile, file: File): PolygonFile?

    @Transactional
    fun saveSolutionFile(solutionFile: SolutionFile, file: File): SolutionFile?

    fun getConditionFile(conditionFile: ConditionFile, version: Long = conditionFile.fileVersion): File?

    fun getExerciseFile(exerciseFile: ExerciseFile, version: Long = exerciseFile.fileVersion): File?

    fun getPolygonFile(polygonFile: PolygonFile, version: Long = polygonFile.fileVersion): File?

    fun getSolutionFile(solutionFile: SolutionFile, version: Long = solutionFile.fileVersion): File?

    fun listFileVersions(file: AbstractFile): List<TaskFileVersionInfo>
}

data class TaskFileVersionInfo(
    val version: Long,
    val fileName: String,
    val lastModifiedAt: Instant,
    val originalName: String
)