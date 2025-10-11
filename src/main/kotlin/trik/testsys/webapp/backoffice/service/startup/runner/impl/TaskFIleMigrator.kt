package trik.testsys.webapp.backoffice.service.startup.runner.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import trik.testsys.webapp.backoffice.data.entity.AbstractFile
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.entity.impl.Task
import trik.testsys.webapp.backoffice.data.entity.impl.TaskFile
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.ConditionFile
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.ExerciseFile
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.PolygonFile
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.SolutionFile
import trik.testsys.webapp.backoffice.data.enums.FileType
import trik.testsys.webapp.backoffice.data.service.TaskFileService
import trik.testsys.webapp.backoffice.data.service.TaskService
import trik.testsys.webapp.backoffice.data.service.VerdictService
import trik.testsys.webapp.backoffice.data.service.impl.taskFile.ConditionFileService
import trik.testsys.webapp.backoffice.data.service.impl.taskFile.ExerciseFileService
import trik.testsys.webapp.backoffice.data.service.impl.taskFile.PolygonFileService
import trik.testsys.webapp.backoffice.data.service.impl.taskFile.SolutionFileService
import trik.testsys.webapp.backoffice.service.FileManager
import trik.testsys.webapp.core.service.startup.AbstractStartupRunner

//@Service
class TaskFIleMigrator(
    private val taskService: TaskService,
    private val taskFileService: TaskFileService,
    private val fileManager: FileManager,
    private val verdictService: VerdictService,

    private val conditionFileService: ConditionFileService,
    private val exerciseFileService: ExerciseFileService,
    private val polygonFileService: PolygonFileService,
    private val solutionFileService: SolutionFileService
) : AbstractStartupRunner() {

    @Transactional(propagation = Propagation.REQUIRED)
    override suspend fun execute() {
        logger.info("Starting TaskFile migration")

        val conditionTaskFiles = mutableSetOf<TaskFile>()
        val exerciseTaskFiles = mutableSetOf<TaskFile>()
        val polygonTaskFiles = mutableSetOf<TaskFile>()
        val solutionTaskFiles = mutableSetOf<TaskFile>()

        val allTaskFiles = taskFileService.findAll().filterNot { it.isRemoved }

        allTaskFiles.forEach { taskFile ->
            when (taskFile.type) {
                TaskFile.TaskFileType.CONDITION -> conditionTaskFiles.add(taskFile)
                TaskFile.TaskFileType.EXERCISE -> exerciseTaskFiles.add(taskFile)
                TaskFile.TaskFileType.POLYGON -> polygonTaskFiles.add(taskFile)
                TaskFile.TaskFileType.SOLUTION -> solutionTaskFiles.add(taskFile)
                else -> error("UNDEFINED")
            }
        }

        logger.info("Found ${conditionTaskFiles.size} condition files, ${exerciseTaskFiles.size} exercise files, ${polygonTaskFiles.size} polygon files, ${solutionTaskFiles.size} solution files")

        val conditionFileMapping = mutableMapOf<Long, Long>()
        conditionTaskFiles.forEach { taskFile ->
            try {
                val conditionFile = createConditionFile(taskFile)
                var savedConditionFile = conditionFileService.save(conditionFile)
                conditionFileMapping[taskFile.id!!] = savedConditionFile.id!!

                var fileVersion = taskFile.fileVersion
                while (fileVersion >= 0) {
                    savedConditionFile.fileVersion++
                    val file = fileManager.getTaskFileVersion(taskFile, fileVersion)!!
                    savedConditionFile = fileManager.saveConditionFile(savedConditionFile, file)!!
                    fileVersion--
                }

                logger.info("Successfully migrated condition file from TaskFile(id=${taskFile.id}) to ConditionFile(id=${savedConditionFile.id})")
            } catch (e: Exception) {
                logger.error("Failed to migrate condition file from TaskFile(id=${taskFile.id})", e)
            }
        }

        // Мигрируем Exercise Files
        val exerciseFileMapping = mutableMapOf<Long, Long>()
        exerciseTaskFiles.forEach { taskFile ->
            try {
                val exerciseFile = createExerciseFile(taskFile)
                var savedExerciseFile = exerciseFileService.save(exerciseFile)
                exerciseFileMapping[taskFile.id!!] = savedExerciseFile.id!!

                var fileVersion = taskFile.fileVersion
                while (fileVersion >= 0) {
                    savedExerciseFile.fileVersion++
                    val file = fileManager.getTaskFileVersion(taskFile, fileVersion)!!
                    savedExerciseFile = fileManager.saveExerciseFile(savedExerciseFile, file)!!
                    fileVersion--
                }

                logger.info("Successfully migrated exercise file from TaskFile(id=${taskFile.id}) to ExerciseFile(id=${savedExerciseFile.id})")
            } catch (e: Exception) {
                logger.error("Failed to migrate exercise file from TaskFile(id=${taskFile.id})", e)
            }
        }

        // Мигрируем Polygon Files
        val polygonFileMapping = mutableMapOf<Long, Long>()
        polygonTaskFiles.forEach { taskFile ->
            try {
                val polygonFile = createPolygonFile(taskFile)
                var savedPolygonFile = polygonFileService.save(polygonFile)
                polygonFileMapping[taskFile.id!!] = savedPolygonFile.id!!

                var fileVersion = taskFile.fileVersion
                while (fileVersion >= 0) {
                    savedPolygonFile.fileVersion++
                    val file = fileManager.getTaskFileVersion(taskFile, fileVersion)!!
                    savedPolygonFile = fileManager.savePolygonFile(savedPolygonFile, file)!!
                    fileVersion--
                }

                logger.info("Successfully migrated polygon file from TaskFile(id=${taskFile.id}) to PolygonFile(id=${savedPolygonFile.id})")
            } catch (e: Exception) {
                logger.error("Failed to migrate polygon file from TaskFile(id=${taskFile.id})", e)
            }
        }

        // Мигрируем Solution Files
        val solutionFileMapping = mutableMapOf<Long, Long>()
        solutionTaskFiles.forEach { taskFile ->
            try {
                val solutionFile = createSolutionFile(taskFile)
                var savedSolutionFile = solutionFileService.save(solutionFile)
                solutionFileMapping[taskFile.id!!] = savedSolutionFile.id!!

                var fileVersion = taskFile.fileVersion
                while (fileVersion >= 0) {
                    savedSolutionFile.fileVersion++
                    val file = fileManager.getTaskFileVersion(taskFile, fileVersion)!!
                    savedSolutionFile = fileManager.saveSolutionFile(savedSolutionFile, file)!!
                    fileVersion--
                }

                logger.info("Successfully migrated solution file from TaskFile(id=${taskFile.id}) to SolutionFile(id=${savedSolutionFile.id})")
            } catch (e: Exception) {
                logger.error("Failed to migrate solution file from TaskFile(id=${taskFile.id})", e)
            }
        }

        val allTasks = taskService.findAll()
        allTasks.forEach { task ->
            logger.debug("Migrating relations with task(id=${task.id})")

            val conditions = task.taskFiles.filter { it.type == TaskFile.TaskFileType.CONDITION && !it.isRemoved }
            val exercises = task.taskFiles.filter { it.type == TaskFile.TaskFileType.EXERCISE && !it.isRemoved }
            val polygons = task.taskFiles.filter { it.type == TaskFile.TaskFileType.POLYGON && !it.isRemoved }
            val solutions = task.taskFiles.filter { it.type == TaskFile.TaskFileType.SOLUTION && !it.isRemoved }

            logger.debug("Task containing conditions(size=${conditions.size}), exercises(size=${exercises.size}), polygons(size=${polygons.size}) and solutions(size=${solutions.size}).")

            val conditionFileIds = conditions.mapNotNull { conditionFileMapping[it.id] }
            val exerciseFileIds = exercises.mapNotNull { exerciseFileMapping[it.id] }
            val polygonFileIds = polygons.mapNotNull { polygonFileMapping[it.id] }

            val solution = task.solutions.filter { it.contest == null }.maxByOrNull { it.id!! }
            val solitonFileScore = solution?.relevantVerdictId?.let {
                val verdict = verdictService.findById(it)
                verdict?.value
            } ?: 0L
            val solutionFileDataById = solutions.associate {
                solutionFileMapping[it.id]!! to Task.SolutionFileData(
                    Solution.SolutionType.QRS,
                    solution?.id,
                    solitonFileScore,
                    solitonFileScore
                )
            }

            task.data.apply {
                this.conditionFileIds.addAll(conditionFileIds)
                this.exerciseFileIds.addAll(exerciseFileIds)
                this.polygonFileIds.addAll(polygonFileIds)
                this.solutionFileDataById.putAll(solutionFileDataById)
            }
            taskService.save(task)

            val conditionFiles = conditionFileService.findAllById(conditionFileIds)
            val exerciseFiles = exerciseFileService.findAllById(exerciseFileIds)
            val polygonFiles = polygonFileService.findAllById(polygonFileIds)
            val solutionFiles = solutionFileService.findAllById(solutionFileDataById.keys)

            conditionFiles.forEach { it.data.attachedTaskIds.add(task.id!!) }
            exerciseFiles.forEach { it.data.attachedTaskIds.add(task.id!!) }
            polygonFiles.forEach { it.data.attachedTaskIds.add(task.id!!) }
            solutionFiles.forEach { it.data.attachedTaskIds.add(task.id!!) }

            conditionFileService.saveAll(conditionFiles)
            exerciseFileService.saveAll(exerciseFiles)
            polygonFileService.saveAll(polygonFiles)
            solutionFileService.saveAll(solutionFiles)

            logger.debug("Successfully migrated relations with task(id=${task.id}). New data: ${task.data}")
        }

        logger.info("TaskFile migration completed")
    }

    private fun createConditionFile(taskFile: TaskFile): ConditionFile {
        return ConditionFile().also {
            it.type = FileType.PDF
            it.name = taskFile.name
//            it.fileVersion = taskFile.fileVersion
            it.fileVersion = -1L
            it.data = AbstractFile.Data(taskFile.data.originalFileNameByVersion.toMutableMap())
            it.developerId = taskFile.developer!!.id
            it.info = taskFile.info
            it.createdAt = taskFile.createdAt
        }
    }

    private fun createExerciseFile(taskFile: TaskFile): ExerciseFile {
        return ExerciseFile().also {
            it.type = FileType.QRS
            it.name = taskFile.name
//            it.fileVersion = taskFile.fileVersion
            it.fileVersion = -1L
            it.data = AbstractFile.Data(taskFile.data.originalFileNameByVersion.toMutableMap())
            it.developerId = taskFile.developer!!.id
            it.info = taskFile.info
            it.createdAt = taskFile.createdAt
        }
    }

    private fun createPolygonFile(taskFile: TaskFile): PolygonFile {
        return PolygonFile().also {
            it.type = FileType.XML
            it.name = taskFile.name
//            it.fileVersion = taskFile.fileVersion
            it.fileVersion = -1L
            it.data = AbstractFile.Data(taskFile.data.originalFileNameByVersion.toMutableMap())
            it.developerId = taskFile.developer!!.id
            it.info = taskFile.info
            it.createdAt = taskFile.createdAt
        }
    }

    private fun createSolutionFile(taskFile: TaskFile): SolutionFile {
        return SolutionFile().also {
            it.type = FileType.QRS
            it.name = taskFile.name
//            it.fileVersion = taskFile.fileVersion
            it.fileVersion = -1L
            it.data = AbstractFile.Data(taskFile.data.originalFileNameByVersion.toMutableMap())
            it.developerId = taskFile.developer!!.id
            it.info = taskFile.info
            it.createdAt = taskFile.createdAt
        }
    }
}