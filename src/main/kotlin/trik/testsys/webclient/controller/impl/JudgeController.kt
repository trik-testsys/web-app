package trik.testsys.webclient.controller.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import trik.testsys.webclient.controller.TrikUserController
import trik.testsys.webclient.entity.impl.Judge
import trik.testsys.webclient.model.impl.JudgeModel
import trik.testsys.webclient.service.impl.JudgeService
import trik.testsys.webclient.service.impl.WebUserService
import trik.testsys.webclient.util.TrikRedirectView
import trik.testsys.webclient.util.logger.TrikLogger

@RestController
@RequestMapping("\${app.testsys.api.prefix}/judge")
class JudgeController @Autowired constructor(
    private val judgeService: JudgeService,
    private val webUserService: WebUserService
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

    private fun validateAccessToken(accessToken: String): Judge? {
        logger.info("Validating access token: $accessToken")
        val webUser = webUserService.getWebUserByAccessToken(accessToken) ?: run {
            logger.info("Access token $accessToken is invalid")
            return null
        }
        return judgeService.getByWebUser(webUser)
    }

    private fun buildModel(judge: Judge): JudgeModel {
        val webUser = judge.webUser

        val judgeModel = JudgeModel.Builder()
            .accessToken(webUser.accessToken)
            .username(webUser.username)
            .additionalInfo(webUser.additionalInfo)
            .registrationDate(webUser.registrationDate)
            .lastLoginDate(webUser.lastLoginDate)
            .build()

        return judgeModel
    }

    companion object {
        private val logger = TrikLogger(JudgeController::class.java)

        private const val ERROR_VIEW_NAME = "error"
        private const val VIEW_NAME = "judge"
        private val REDIRECT_VIEW = TrikRedirectView(VIEW_NAME)
    }
}