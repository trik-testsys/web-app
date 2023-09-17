package trik.testsys.webclient.controller.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView
import trik.testsys.webclient.controller.TrikUserController
import trik.testsys.webclient.entity.*

import trik.testsys.webclient.model.impl.ViewerModel
import trik.testsys.webclient.service.impl.GroupService
import trik.testsys.webclient.service.impl.LabelService
import trik.testsys.webclient.service.impl.ViewerService
import trik.testsys.webclient.service.impl.WebUserService
import trik.testsys.webclient.util.fp.Either
import trik.testsys.webclient.util.logger.TrikLogger

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@RestController
@RequestMapping("\${app.testsys.api.prefix}/viewer")
class ViewerController @Autowired constructor(
    private val webUserService: WebUserService,
    private val groupService: GroupService,
    private val viewerService: ViewerService,
    private val labelService: LabelService
) : TrikUserController {

    @GetMapping
    override fun getAccess(
        @RequestParam accessToken: String,
        modelAndView: ModelAndView
    ): ModelAndView {
        logger.info(accessToken, "Client trying to access viewer page.")

        val eitherViewer = validateViewer(accessToken)
        if (eitherViewer.isLeft()) {
            return eitherViewer.getLeft()
        }
        val viewer = eitherViewer.getRight()

        val viewerModel = ViewerModel.Builder()
            .accessToken(accessToken)
            .username(viewer.webUser.username)
            .build()
        modelAndView.viewName = VIEWER_VIEW_NAME
        modelAndView.addAllObjects(viewerModel.asMap())

        return modelAndView
    }

    @GetMapping("/result")
    fun getResults(
        @RequestParam accessToken: String,
        @RequestParam labelNames: List<String>,
        @RequestParam intersection: Boolean,
        modelAndView: ModelAndView
    ): ModelAndView {
        logger.info(accessToken, "Client trying to get results by labels: ${labelNames}.")

        val eitherViewer = validateViewer(accessToken)
        if (eitherViewer.isLeft()) {
            return eitherViewer.getLeft()
        }
        val viewer = eitherViewer.getRight()

        val labels = mutableSetOf<Label>()
        val correctNames = mutableSetOf<String>()
        val incorrectNames = mutableSetOf<String>()
        labelNames.forEach { labelName ->
            val label = labelService.getByName(labelName)
            if (label != null) {
                correctNames.add(labelName)
                labels.add(label)
            } else {
                incorrectNames.add(labelName)
            }
        }

        val groups = if (intersection) {
            labelService.getGroupsWithAllLabels(labels)
        } else {
            labelService.getGroupsWithAnyLabel(labels)
        }

        val tasksSet = mutableSetOf<Task>()
        groups.forEach { group ->
            tasksSet.addAll(group.tasks)
        }

        val studentsSet = mutableSetOf<Student>()
        groups.forEach { group ->
            studentsSet.addAll(group.students)
        }

        val tasksList = tasksSet.toList()
        val table = mutableListOf<TableRow>()
        studentsSet.forEach { student ->
            val tasksInfo = mutableListOf<Long>()
            tasksList.forEach { task ->
                val failedSolution =
                    student.solutions.find { it.task == task && (it.status == Solution.Status.FAILED || it.status == Solution.Status.ERROR) }
                val passedSolution =
                    student.solutions.find { it.task == task && it.status == Solution.Status.PASSED }
                val inProgressSolution =
                    student.solutions.find { it.task == task && (it.status == Solution.Status.IN_PROGRESS || it.status == Solution.Status.NOT_STARTED) }

                if (failedSolution != null) {
                    tasksInfo.add(0)
                } else if (passedSolution != null) {
                    tasksInfo.add(1)
                } else if (inProgressSolution != null) {
                    tasksInfo.add(2)
                } else {
                    tasksInfo.add(-1)
                }
            }

            val username = student.webUser.username
            val groupName = student.group.name
            val tableRow = TableRow(groupName, student.id!!, username, tasksInfo)
            table.add(tableRow)
        }


        val viewerModel = ViewerModel.Builder()
            .accessToken(accessToken)
            .correctNames(correctNames)
            .incorrectNames(incorrectNames)
            .labels(labels)
            .table(table)
            .build()

        modelAndView.viewName = VIEWER_VIEW_NAME
        modelAndView.addAllObjects(viewerModel.asMap())
        return modelAndView
    }

    data class TableRow(
        val username: String,
        val tasksInfo: List<Long>
    ) {
        constructor(
            groupName: String, studentId: Long, studentName: String,
            tasksInfo: List<Long>
        ) : this(
            "$groupName|$studentId|$studentName",
            tasksInfo
        )
    }

    private fun validateViewer(accessToken: String): Either<ModelAndView, Viewer> {
        val modelAndView = ModelAndView("error")
        val webUser = webUserService.getWebUserByAccessToken(accessToken) ?: run {
            logger.info(accessToken, "Client is not found.")
            modelAndView.addObject("message", "Client not found.")

            return Either.left(modelAndView)
        }

        val viewer = viewerService.getByWebUser(webUser) ?: run {
            logger.info(accessToken, "Client is not a viewer.")
            modelAndView.addObject("message", "Client is not a viewer.")

            return Either.left(modelAndView)
        }

        logger.info(accessToken, "Client is a viewer.")
        return Either.right(viewer)
    }

    companion object {
        private val logger = TrikLogger(this::class.java)

        private const val VIEWER_VIEW_NAME = "viewer"
    }
}