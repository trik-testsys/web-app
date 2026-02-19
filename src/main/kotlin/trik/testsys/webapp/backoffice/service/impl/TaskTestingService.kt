package trik.testsys.webapp.backoffice.service.impl

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.entity.impl.Task
import trik.testsys.webapp.backoffice.data.entity.impl.Verdict
import trik.testsys.webapp.backoffice.data.service.SolutionService
import trik.testsys.webapp.backoffice.data.service.TaskService
import trik.testsys.webapp.backoffice.data.service.VerdictService

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
@Service
class TaskTestingService(
    private val taskService: TaskService,
    private val solutionService: SolutionService,
    private val verdictService: VerdictService,
) {

    @Scheduled(fixedRate = 60_000)
    fun updateTaskTestingStatus() {
        logger.debug("Started updateTaskTestingStatus")
        val allTestingTasks = taskService.findAllTesting()

        logger.debug("Found ${allTestingTasks.size} testing tasks.")

        for (task in allTestingTasks) {
            logger.debug("Started updateTaskTestingStatus for task(id=${task.id})")

            val solutionIds = task.data.solutionFileDataById.values.mapNotNull { fileData ->
                fileData.lastSolutionId
            }

            val solutions = solutionService.findAllById(solutionIds)

            val allGraded = solutions.all {
                it.status == Solution.Status.PASSED ||
                it.status == Solution.Status.ERROR  ||
                it.status == Solution.Status.TIMEOUT
            }

            if (!allGraded) {
                logger.debug("Not all solution are graded. Skipping updateTaskTestingStatus for task(id=${task.id})")
                continue
            }
            logger.debug("All solution are graded. Continuing updateTaskTestingStatus for task(id=${task.id})")

            val verdictBySolutionId = verdictService.findAllBySolutionIds(solutions.mapNotNull { it.id })
                .associateBy { it.solutionId }
            updateTask(task, verdictBySolutionId)

            logger.debug("Finished updateTaskTestingStatus for task(id=${task.id}, newStatus=${task.testingStatus}, data=${task.data})")
        }

        logger.debug("Finished updateTaskTestingStatus")
    }

    private fun updateTask(task: Task, verdictBySolutionId: Map<Long, Verdict>) {
        val newStatus = getNewStatus(task, verdictBySolutionId)
        task.testingStatus = newStatus

        updateTaskData(task, verdictBySolutionId)

        taskService.save(task)
    }

    private fun getNewStatus(task: Task, verdictBySolutionId: Map<Long, Verdict>): Task.TestingStatus {
        val allWithExpectedScore = task.data.solutionFileDataById.values.all { fileData ->
            val verdict = verdictBySolutionId[fileData.lastSolutionId] ?: run {
                val message = "Missing verdict(solutionId=${fileData.lastSolutionId})"
                logger.error(message)
                throw IllegalStateException(message)
            }

            verdict.value == fileData.score
        }

        return if (allWithExpectedScore) Task.TestingStatus.PASSED else Task.TestingStatus.FAILED
    }

    private fun updateTaskData(task: Task, verdictBySolutionId: Map<Long, Verdict>) {
        task.data.solutionFileDataById.values.forEach { fileData ->
            val verdict = verdictBySolutionId[fileData.lastSolutionId] ?: run {
                val message = "Missing verdict(solutionId=${fileData.lastSolutionId})"
                logger.error(message)
                throw IllegalStateException(message)
            }

            fileData.lastTestScore = verdict.value
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(TaskTestingService::class.java)
    }
}