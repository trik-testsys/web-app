package trik.testsys.webapp.backoffice.service.startup.runner.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.transaction.support.TransactionTemplate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.entity.impl.Task
import trik.testsys.webapp.backoffice.data.service.ContestService
import trik.testsys.webapp.backoffice.data.service.SolutionService
import trik.testsys.webapp.backoffice.data.service.TaskService
import trik.testsys.webapp.backoffice.data.service.VerdictService
import trik.testsys.webapp.backoffice.service.FileManager
import trik.testsys.webapp.backoffice.service.Grader
import trik.testsys.webapp.core.service.startup.AbstractStartupRunner
import trik.testsys.webapp.notifier.CombinedIncidentNotifier
import java.io.File
import java.util.regex.Pattern
import javax.annotation.PostConstruct

@Service
class GraderInitializer(
    private val grader: Grader,
    private val fileManager: FileManager,
    private val solutionService: SolutionService,
    private val taskService: TaskService,
    private val contestService: ContestService,
    private val verdictService: VerdictService,
    private val transactionTemplate: TransactionTemplate,

    @Value("\${trik.testsys.trik-studio.container.name}") private val trikStudioContainerName: String,
    @Value("\${trik.testsys.grading-node.addresses}") private val gradingNodeAddresses: String, private val notifier: CombinedIncidentNotifier
) : AbstractStartupRunner() {
    
    @PostConstruct
    fun init() {
        if (trikStudioContainerName.trim().isEmpty()) {
            throw IllegalStateException("TRIK Studio container name must be defined.")
        }
        if (gradingNodeAddresses.trim().isEmpty()) {
            throw IllegalStateException("Grading node addresses must be defined.")
        }
    }

    override suspend fun execute() {
        addGraderNodes()
        addGraderSubscription()
        sendToGradeUngradedSolutions()
    }
    
    private fun sendToGradeUngradedSolutions() {
        logger.info("Sending ungraded solutions to grade...")

        val ungradedSolutions = solutionService.findAll()
            .filter { it.status == Solution.Status.NOT_STARTED || it.status == Solution.Status.IN_PROGRESS }
            .filter {
                val file = fileManager.getSolutionFile(it)
                if (file == null) {
                    logger.error("Solution file for solution ${it.id} is missing.")

                    val managed = solutionService.getById(requireNotNull(it.id))
                    managed.status = Solution.Status.ERROR
                    verdictService.createNewForSolution(managed, 0)

//                    if (managed.isLastTaskTest()) changeTaskTestingResult(managed)
                    solutionService.save(managed)

                    false
                } else {
                    true
                }
            }

        logger.info("Found ${ungradedSolutions.size} ungraded solutions.")

        ungradedSolutions.forEach {
            grader.sendToGrade(it, Grader.GradingOptions(true, trikStudioContainerName))
        }
    }

    private fun addGraderNodes() {
        logger.info("Adding grader nodes...")

        val parsedAddresses = gradingNodeAddresses.split(",")
        logger.info("Parsed addresses: $parsedAddresses")

        parsedAddresses.forEach { grader.addNode(it) }

        logger.info("Grader nodes were added.")
    }

    private fun addGraderSubscription() = grader.subscribeOnGraded { gradingInfo ->
        logger.info("Grading info was received for solutionId: ${gradingInfo.submissionId}")

        transactionTemplate.execute {
            try {
                when (gradingInfo) {
                    is Grader.GradingInfo.Ok -> gradingInfo.parse()
                    is Grader.GradingInfo.Error -> {
                        notifyGradingError(gradingInfo)
                        gradingInfo.parse()
                    }
                }
            } catch (e: Exception) {
                logger.error("Failed to parse grading info.", e)

                afterCatchException(gradingInfo)
            }
        }
    }

    private fun notifyGradingError(error: Grader.GradingInfo.Error) {
        val kind = error.kind
        val errorDescription = when (kind) {
            is Grader.ErrorKind.InnerTimeoutExceed -> null
            is Grader.ErrorKind.NonZeroExitCode -> {
                "non-zero exit code[${kind.code}]" + when (kind.code) {
                    137 -> ", probably out of memory"
                    125 -> ", probably invalid docker arguments"
                    else -> ""
                }
            }
            else -> "[ ${kind::class.java.simpleName} ]: ${kind.description}"
        } ?: return

        val message = "Error while grading solution(id=${error.submissionId}):\n\n $errorDescription"

        notifier.notify(message)
    }

    private fun afterCatchException(gradingInfo: Grader.GradingInfo) {
        try {
            val solutionId = gradingInfo.submissionId
            val solution = solutionService.findById(solutionId.toLong()) ?: return
            val managed = solutionService.getById(requireNotNull(solution.id))

            managed.status = Solution.Status.ERROR

            verdictService.createNewForSolution(managed, 0)

            solutionService.save(managed)
        } catch (e: Exception) {
            logger.error("Failed to handle exception.", e)
        }
    }

    private fun Grader.GradingInfo.Ok.parse() = let { (solutionId, _) ->
        logger.info("Solution $solutionId was graded without errors.")
        fileManager.saveSuccessfulGradingInfo(this)

        val solution = solutionService.findById(solutionId.toLong()) ?: return@let
        val verdicts = fileManager.getVerdictFiles(solution)

        val objectMapper = createMapper()

        var allFailed = true
        var totalScore = 0L

        verdicts.forEach { verdict ->
            val elements = objectMapper.readVerdictElements(verdict) ?: run {
                logger.error("Failed to read verdict elements from file $verdict.")

                return@forEach
            }

            val infoElements = elements.filter { it.level == VerdictElement.LEVEL_INFO }
            val errorElements = elements.filter { it.level == VerdictElement.LEVEL_ERROR }

            if (errorElements.isNotEmpty()) return@forEach

            val score = infoElements
                .filter { (_, message) -> VerdictElement.SCORE_PATTERN.matcher(message).find() }
                .mapNotNull { (_, message) -> VerdictElement.matchScore(message) }
                .maxOrNull() ?: return@forEach

            allFailed = false
            totalScore += score
        }

        val managed = solutionService.getById(requireNotNull(solution.id))
        managed.status = Solution.Status.PASSED
        verdictService.createNewForSolution(managed, totalScore)
        solutionService.save(managed)

        // When solution belongs to developer task testing (contest == null), update task.testingStatus
        if (managed.contest == null) {
            val task = managed.task
            // Determine if all test solutions for this task have finished
            val allSolutions = solutionService.findAll().filter { it.task.id == task.id && it.contest == null }
            val anyInProgress = allSolutions.any { it.status == Solution.Status.NOT_STARTED || it.status == Solution.Status.IN_PROGRESS }
            if (!anyInProgress) {
                task.testingStatus = Task.TestingStatus.PASSED
                taskService.save(task)
            }
        }

    }

//    private fun changeTaskTestingResult(solution: Solution) {
//        if (solution.status == Solution.Status.PASSED || !solution.task.hasExercise || !solution.task.hasSolution || solution.task.polygonsCount == 0L) {
//            solution.task.fail()
//
//            solution.task.contests.forEach {
//                it.tasks.remove(solution.task)
//                contestService.save(it)
//            }
//            solution.task.contests.clear()
//        }
//
//        if (solution.status == Solution.Status.PASSED && solution.task.hasExercise && solution.task.hasSolution && solution.task.polygonsCount > 0L) {
//            solution.task.pass()
//        }
//        taskService.save(solution.task)
//    }

    private fun ObjectMapper.readVerdictElements(verdict: File): List<VerdictElement>? = try {
        readValue(verdict, object : TypeReference<List<VerdictElement>>() {}) ?: run {
            logger.error("Failed to read verdict elements from file $verdict.")
            null
        }
    } catch (e: Exception) {
        logger.error("Failed to read verdict elements from file $verdict.", e)
        null
    }

    private fun createMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return objectMapper
    }

//    @XmlRootElement
    private data class VerdictElement(
        val level: String = "",
        val message: String = ""
    ) {

        companion object {

            val LEVEL_INFO = "info"
            val LEVEL_ERROR = "error"

            // regex for strings like "Успешно пройдено. Набрано баллов: 100."
            val SCORE_PATTERN = Pattern.compile("Набрано баллов:\\s*(\\d+)")

            fun matchScore(message: String): Long? {
                val matcher = SCORE_PATTERN.matcher(message)
                return if (matcher.find()) matcher.group(1).toLong() else null
            }
        }
    }


    private fun Grader.GradingInfo.Error.parse() = let { (solutionId, kind) ->
        logger.info("Solution $solutionId was graded with error: $kind.")
        val solution = solutionService.findById(solutionId.toLong()) ?: return@let
        val managed = solutionService.getById(requireNotNull(solution.id))

        managed.status = when (kind) {
            is Grader.ErrorKind.InnerTimeoutExceed -> Solution.Status.TIMEOUT
            else -> Solution.Status.ERROR
        }

        verdictService.createNewForSolution(managed, 0)
        solutionService.save(managed)

        if (managed.contest == null) {
            val task = managed.task
            task.testingStatus = Task.TestingStatus.FAILED
            taskService.save(task)
        }
    }
    
    companion object {

        private val logger = LoggerFactory.getLogger(GraderInitializer::class.java)
    }
}