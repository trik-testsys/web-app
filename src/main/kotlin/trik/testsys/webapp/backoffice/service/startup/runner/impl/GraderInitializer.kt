package trik.testsys.webapp.backoffice.service.startup.runner.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.PostLoad
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.service.ContestService
import trik.testsys.webapp.backoffice.data.service.SolutionService
import trik.testsys.webapp.backoffice.data.service.TaskService
import trik.testsys.webapp.backoffice.data.service.VerdictService
import trik.testsys.webapp.backoffice.service.FileManager
import trik.testsys.webapp.backoffice.service.Grader
import trik.testsys.webapp.core.service.startup.AbstractStartupRunner
import java.io.File
import java.util.regex.Pattern

@Service
class GraderInitializer(
    private val grader: Grader,
    private val fileManager: FileManager,
    private val solutionService: SolutionService,
    private val taskService: TaskService,
    private val contestService: ContestService,
    private val verdictService: VerdictService,


    @Value("\${trik.testsys.trik-studio.container.name}") private val trikStudioContainerName: String,
    @Value("\${trik.testsys.grading-node.addresses}") private val gradingNodeAddresses: String
) : AbstractStartupRunner() {
    
    @PostLoad
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

        try {
            when (gradingInfo) {
                is Grader.GradingInfo.Ok -> gradingInfo.parse()
                is Grader.GradingInfo.Error -> gradingInfo.parse()
            }
        } catch (e: Exception) {
            logger.error("Failed to parse grading info.", e)

            afterCatchException(gradingInfo)
        }
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

        solution.status = if (allFailed) Solution.Status.FAILED else Solution.Status.PASSED
        verdictService.createNewForSolution(solution, totalScore)
        solutionService.save(solution)

//        if (managed.isLastTaskTest()) changeTaskTestingResult(managed)

    }

//    private fun changeTaskTestingResult(solution: Solution) {
//        if (solution.status == Solution.Status.FAILED || !solution.task.hasExercise || !solution.task.hasSolution || solution.task.polygonsCount == 0L) {
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
            is Grader.ErrorKind.InnerTimeoutExceed -> Solution.Status.FAILED
            else -> Solution.Status.ERROR
        }

        verdictService.createNewForSolution(managed, 0)
        solutionService.save(managed)
    }
    
    companion object {

        private val logger = LoggerFactory.getLogger(GraderInitializer::class.java)
    }
}