package trik.testsys.webclient.controller.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import trik.testsys.webclient.controller.TrikUserController
import trik.testsys.webclient.entity.impl.Judge
import trik.testsys.webclient.model.impl.JudgeModel
import trik.testsys.webclient.service.impl.JudgeService
import trik.testsys.webclient.service.impl.SolutionService
import trik.testsys.webclient.service.impl.TaskActionService
import trik.testsys.webclient.service.impl.WebUserService
import trik.testsys.webclient.util.TrikRedirectView
import trik.testsys.webclient.util.logger.TrikLogger
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC

@RestController
@RequestMapping("\${app.testsys.api.prefix}/judge")
class JudgeController @Autowired constructor(
    private val judgeService: JudgeService,
    private val webUserService: WebUserService,
    private val solutionService: SolutionService,
    private val taskActionService: TaskActionService
) : TrikUserController {

    @GetMapping
    override fun getAccess(
        @RequestParam accessToken: String,
        modelAndView: ModelAndView
    ): ModelAndView {
        logger.info(accessToken, "Client requested access to judge page")
        val judge = validateAccessToken(accessToken) ?: run {
            logger.info(accessToken, "Client access token is invalid")
            modelAndView.viewName = ERROR_VIEW_NAME
            return modelAndView
        }
        logger.info(accessToken, "Client access token is valid")

        val model = buildModel(judge)
        modelAndView.viewName = VIEW_NAME
        modelAndView.addAllObjects(model.asMap())

        return modelAndView
    }

    @PostMapping("/solution/edit")
    fun editSolutionScore(
        @RequestParam accessToken: String,
        @RequestParam studentId: Long?,
        @RequestParam groupId: Long?,
        @RequestParam adminId: Long?,
        @RequestParam taskId: Long?,
        @RequestParam solutionId: Long?,

        @RequestParam solutionToChangeId: Long,
        @RequestParam newScore: Long,
        modelAndView: ModelAndView
    ): ModelAndView {
        logger.info(accessToken, "Client requested solutions")
        val judge = validateAccessToken(accessToken) ?: run {
            logger.info(accessToken, "Client access token is invalid")
            modelAndView.viewName = ERROR_VIEW_NAME
            return modelAndView
        }
        logger.info(accessToken, "Client access token is valid")

        val filterParams = mapOf(
            "studentId" to studentId,
            "groupId" to groupId,
            "adminId" to adminId,
            "taskId" to taskId,
            "solutionId" to solutionId
        )

        val solutionToChange = solutionService.getById(solutionToChangeId) ?: run {
            logger.info(accessToken, "Solution with id $solutionToChangeId not found")
            modelAndView.viewName = ERROR_VIEW_NAME
            return modelAndView
        }
        solutionToChange.score = newScore
        solutionService.saveSolution(solutionToChange)

        val allSolutions = solutionService.getAllSolutions()
        val filteredSolutions = allSolutions.filter { solution ->
            (studentId == null || solution.student.id == studentId) &&
                    (groupId == null || solution.student.group.id == groupId) &&
                    (adminId == null || solution.student.group.admin.id == adminId) &&
                    (taskId == null || solution.task.id == taskId) &&
                    (solutionId == null || solution.id == solutionId)
        }
        val solutions = filteredSolutions.map { solution ->
            val uploadedTaskAction = taskActionService.getUploadedSolutionAction(solution.student, solution)
            val downloadedTaskAction = taskActionService.getDownloadedTrainingAction(solution.student, solution.task)

            val isInTime = if (uploadedTaskAction == null || downloadedTaskAction == null) {
                false
            } else {
                uploadedTaskAction.dateTime.isBefore(downloadedTaskAction.dateTime.plusSeconds(maxTimeToSolve)) ||
                        uploadedTaskAction.dateTime.isEqual(downloadedTaskAction.dateTime.plusSeconds(maxTimeToSolve))
            }

            SolutionRow(
                solution.student.id!!,
                solution.student.group.id!!,
                solution.student.group.admin.id!!,
                solution.task.id!!,
                solution.id!!,
                solution.score,
                uploadedTaskAction?.dateTime?.plusHours(3)
                    ?: solution.date.toInstant().atZone(UTC).toLocalDateTime().plusHours(3),
                isInTime
            )
        }

        val model = buildModel(judge, solutions, filterParams)
        modelAndView.viewName = VIEW_NAME
        modelAndView.addAllObjects(model.asMap())

        return modelAndView
    }

    @GetMapping("/solutions")
    fun getSolutions(
        @RequestParam accessToken: String,
        @RequestParam studentId: Long?,
        @RequestParam groupId: Long?,
        @RequestParam adminId: Long?,
        @RequestParam taskId: Long?,
        @RequestParam solutionId: Long?,
        modelAndView: ModelAndView
    ): ModelAndView {
        logger.info(accessToken, "Client requested solutions")
        val judge = validateAccessToken(accessToken) ?: run {
            logger.info(accessToken, "Client access token is invalid")
            modelAndView.viewName = ERROR_VIEW_NAME
            return modelAndView
        }
        logger.info(accessToken, "Client access token is valid")

        val filterParams = mapOf(
            "studentId" to studentId,
            "groupId" to groupId,
            "adminId" to adminId,
            "taskId" to taskId,
            "solutionId" to solutionId
        )

        val allSolutions = solutionService.getAllSolutions()
        val filteredSolutions = allSolutions.filter { solution ->
            (studentId == null || solution.student.id == studentId) &&
            (groupId == null || solution.student.group.id == groupId) &&
            (adminId == null || solution.student.group.admin.id == adminId) &&
            (taskId == null || solution.task.id == taskId) &&
            (solutionId == null || solution.id == solutionId)
        }
        val solutions = filteredSolutions.map { solution ->
            val uploadedTaskAction = taskActionService.getUploadedSolutionAction(solution.student, solution)
            val downloadedTaskAction = taskActionService.getDownloadedTrainingAction(solution.student, solution.task)

            val isInTime = if (uploadedTaskAction == null || downloadedTaskAction == null) {
                false
            } else {
                uploadedTaskAction.dateTime.isBefore(downloadedTaskAction.dateTime.plusSeconds(maxTimeToSolve)) ||
                uploadedTaskAction.dateTime.isEqual(downloadedTaskAction.dateTime.plusSeconds(maxTimeToSolve))
            }

            SolutionRow(
                solution.student.id!!,
                solution.student.group.id!!,
                solution.student.group.admin.id!!,
                solution.task.id!!,
                solution.id!!,
                solution.score,
                uploadedTaskAction?.dateTime?.plusHours(3)
                    ?: solution.date.toInstant().atZone(UTC).toLocalDateTime().plusHours(3),
                isInTime
            )
        }

        val model = buildModel(judge, solutions, filterParams)
        modelAndView.viewName = VIEW_NAME
        modelAndView.addAllObjects(model.asMap())

        return modelAndView
    }

    data class SolutionRow(
        val studentId: Long,
        val groupId: Long,
        val adminId: Long,
        val taskId: Long,
        val solutionId: Long,
        val score: Long,
        val dateTime: LocalDateTime,
        val isInTime: Boolean
    )

    private fun validateAccessToken(accessToken: String): Judge? {
        logger.info("Validating access token: $accessToken")
        val webUser = webUserService.getWebUserByAccessToken(accessToken) ?: run {
            logger.info("Access token $accessToken is invalid")
            return null
        }
        return judgeService.getByWebUser(webUser)
    }

    private fun buildModel(
        judge: Judge,
        solutions: List<SolutionRow>? = null,
        filterParams: Map<String, Any?>? = null
    ): JudgeModel {
        val webUser = judge.webUser

        val judgeModel = JudgeModel.Builder()
            .accessToken(webUser.accessToken)
            .username(webUser.username)
            .additionalInfo(webUser.additionalInfo)
            .registrationDate(webUser.registrationDate)
            .lastLoginDate(webUser.lastLoginDate)
            .solutions(solutions)
            .filterParams(filterParams)
            .build()

        return judgeModel
    }

    companion object {
        private val logger = TrikLogger(JudgeController::class.java)

        private const val ERROR_VIEW_NAME = "error"
        private const val VIEW_NAME = "judge"
        private val REDIRECT_VIEW = TrikRedirectView("/$VIEW_NAME")

        private val maxTimeToSolve = 90 * 60L
    }
}