package trik.testsys.webclient.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.ModelAndView

import trik.testsys.webclient.util.handler.GradingSystemErrorHandler
import trik.testsys.webclient.entity.Developer
import trik.testsys.webclient.entity.WebUser
import trik.testsys.webclient.service.DeveloperService
import trik.testsys.webclient.service.TaskService
import trik.testsys.webclient.service.WebUserService
import trik.testsys.webclient.util.fp.Either
import trik.testsys.webclient.util.logger.TrikLogger

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@RestController
@RequestMapping("\${app.testsys.api.prefix}/developer")
class DeveloperController @Autowired constructor(
    private val developerService: DeveloperService,
    private val webUserService: WebUserService,
    private val taskService: TaskService
) {

    private val restTemplate = RestTemplate()

    @GetMapping("/test")
    fun test(): ModelAndView {
        val modelAndView = ModelAndView("developer")
        modelAndView.addObject("username", "Roman")
        modelAndView.addObject("accessToken", "ed30da0f75d595465d6977e2fd551d2026cc3ff66dd5bd958ac2a50807684cb7")
        println(modelAndView.model)
        return modelAndView
    }

    @GetMapping
    fun getAccess(
        @RequestParam accessToken: String,
        modelAndView: ModelAndView
    ): ModelAndView {
        logger.info(accessToken, "Client trying to access developer page.")

        val isDeveloper = validateDeveloper(accessToken)
        if (isDeveloper.isLeft()) {
            return isDeveloper.getLeft()
        }

        modelAndView.viewName = "developer"
        modelAndView.addObject("accessToken", accessToken)
        modelAndView.addObject("username", webUserService.getWebUserByAccessToken(accessToken)?.username)
        return modelAndView
    }

    @GetMapping("/task/create")
    fun createTask(
        @RequestParam accessToken: String,
        @RequestParam name: String,
        @RequestParam description: String,
        @RequestBody tests: List<MultipartFile>,
        @RequestBody benchmark: MultipartFile?,
        @RequestBody training: MultipartFile?,
        modelAndView: ModelAndView
    ): ModelAndView {
        logger.info(accessToken, "Client trying to create task.")

        val eitherDeveloperEntities = validateDeveloper(accessToken)
        if (eitherDeveloperEntities.isLeft()) {
            return eitherDeveloperEntities.getLeft()
        }

        val webUser = eitherDeveloperEntities.getRight().webUser
        val developer = eitherDeveloperEntities.getRight().developer

        modelAndView.addObject("accessToken", accessToken)

        restTemplate.errorHandler = GradingSystemErrorHandler()
        val testsCount = tests.size.toLong()
        val task = taskService.saveTask(name, description, testsCount, developer, training, benchmark)

        val headers = org.springframework.http.HttpHeaders()

        TODO()
    }

    private fun validateDeveloper(accessToken: String): Either<ModelAndView, DeveloperEntities> {
        val modelAndView = ModelAndView("error")
        val webUser = webUserService.getWebUserByAccessToken(accessToken) ?: run {
            logger.info(accessToken, "Client not found.")
            modelAndView.addObject("message", "Client not found.")

            return Either.left(modelAndView)
        }
        val developer = developerService.getByWebUser(webUser) ?: run {
            logger.info(accessToken, "Client is not a developer.")
            modelAndView.addObject("message", "You are not a developer.")

            return Either.left(modelAndView)
        }
        val developerEntities = DeveloperEntities(developer, webUser)

        logger.info(accessToken, "Client is a developer.")
        return Either.right(developerEntities)
    }

    data class DeveloperEntities(
        val developer: Developer,
        val webUser: WebUser,
    )

    companion object {
        private val logger = TrikLogger(this::class.java)
    }
}