package trik.testsys.webclient.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView
import trik.testsys.webclient.entity.Label

import trik.testsys.webclient.entity.Viewer
import trik.testsys.webclient.model.ViewerModel
import trik.testsys.webclient.service.GroupService
import trik.testsys.webclient.service.LabelService
import trik.testsys.webclient.service.ViewerService
import trik.testsys.webclient.service.WebUserService
import trik.testsys.webclient.util.fp.Either
import trik.testsys.webclient.util.logger.TrikLogger

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
        logger.info(accessToken, "Client trying to access viewer page.")

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

        val viewerModel = ViewerModel.Builder()
            .accessToken(accessToken)
            .correctNames(correctNames)
            .incorrectNames(incorrectNames)
            .labels(labels)
            .build()

        modelAndView.viewName = VIEWER_VIEW_NAME
        modelAndView.addAllObjects(viewerModel.asMap())
        return modelAndView
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