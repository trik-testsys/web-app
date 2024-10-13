package trik.testsys.webclient.service.startup.runner.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import trik.testsys.webclient.entity.impl.Solution
import trik.testsys.webclient.service.FileManager
import trik.testsys.webclient.service.Grader
import trik.testsys.webclient.service.entity.impl.ContestService
import trik.testsys.webclient.service.entity.impl.SolutionService
import trik.testsys.webclient.service.entity.impl.TaskService
import trik.testsys.webclient.service.startup.runner.StartupRunner
import java.io.File
import java.util.regex.Pattern
import javax.xml.bind.annotation.XmlRootElement

@Service
class GradingInfoParserRunner(
    private val grader: Grader,
    private val fileManager: FileManager,
    private val solutionService: SolutionService,
    private val taskService: TaskService,
    private val contestService: ContestService
) : StartupRunner {

    override suspend fun run() {
        TODO("Not yet implemented")
    }

    override fun runBlocking() {
        addGraderNodes()
        addGraderSubscription()
    }

    private fun addGraderNodes() {
        logger.info("Adding grader nodes...")

        grader.addNode("87.228.25.186:8080")

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
            val solution = solutionService.find(solutionId.toLong()) ?: return

            solution.status = Solution.SolutionStatus.ERROR
            solution.score = 0

            solutionService.save(solution)
        } catch (e: Exception) {
            logger.error("Failed to handle exception.", e)
        }
    }

    private fun Grader.GradingInfo.Ok.parse() = let { (solutionId, _) ->
        logger.info("Solution $solutionId was graded without errors.")
        fileManager.saveSuccessfulGradingInfo(this)

        val solution = solutionService.find(solutionId.toLong()) ?: return@let
        val verdicts = fileManager.getVerdictFiles(solution)

        val objectMapper = createMapper()
        verdicts.forEach { verdict ->
            val elements = objectMapper.readVerdictElements(verdict) ?: run {
                logger.error("Failed to read verdict elements from file $verdict.")

                solution.status = Solution.SolutionStatus.FAILED
                solution.score = 0

                return@forEach
            }

            val infoElements = elements.filter { it.level == VerdictElement.LEVEL_INFO }
            val errorElements = elements.filter { it.level == VerdictElement.LEVEL_ERROR }

            if (errorElements.isNotEmpty()) {
                solution.status = Solution.SolutionStatus.FAILED
                solution.score = 0

                return@forEach
            }

            val score = infoElements
                .filter { (_, message) -> VerdictElement.SCORE_PATTERN.matcher(message).find() }
                .mapNotNull { (_, message) -> VerdictElement.matchScore(message) }
                .maxOrNull()
                ?: run {

                    solution.status = Solution.SolutionStatus.FAILED
                    solution.score = 0

                    return@forEach
                }

            solution.status = Solution.SolutionStatus.PASSED
            solution.score = score
        }

        if (solution.student == null) {
            if (solution.status == Solution.SolutionStatus.PASSED) {
                solution.task.pass()
            }

            if (solution.status == Solution.SolutionStatus.FAILED) {
                solution.task.fail()

                solution.task.contests.forEach {
                    it.tasks.remove(solution.task)
                    contestService.save(it)
                }
                solution.task.contests.clear()
            }
            taskService.save(solution.task)
        }

        solutionService.save(solution)
    }

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

    @XmlRootElement
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


    private fun Grader.GradingInfo.Error.parse() = let { (solutionId, kind, description) ->
        logger.info("Solution $solutionId was graded with error: $kind. Description: $description.")
        val solution = solutionService.find(solutionId.toLong()) ?: return@let

        // timeout error
        if (kind == 4) {
            solution.status = Solution.SolutionStatus.FAILED
            solution.score = 0
        } else {
            solution.status = Solution.SolutionStatus.ERROR
            solution.score = 0
        }

        solutionService.save(solution)
    }

    companion object {

        private val logger = LoggerFactory.getLogger(GradingInfoParserRunner::class.java)
    }
}