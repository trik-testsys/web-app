package trik.testsys.webclient.controller.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView
import trik.testsys.webclient.controller.TrikUserController
import trik.testsys.webclient.service.impl.JudgeService
import trik.testsys.webclient.util.logger.TrikLogger

@RestController
@RequestMapping("\${app.testsys.api.prefix}/judge")
class JudgeController @Autowired constructor(
    private val judgeService: JudgeService
) : TrikUserController {

    @GetMapping
    override fun getAccess(
        @RequestParam accessToken: String,
        modelAndView: ModelAndView
    ): ModelAndView {
        TODO("Not yet implemented")
    }

    companion object {
        private val logger = TrikLogger(JudgeController::class.java)
    }
}