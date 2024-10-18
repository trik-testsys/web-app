package trik.testsys.webclient.controller.impl.user.judge

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import trik.testsys.webclient.controller.impl.user.judge.JudgeMainController.Companion.JUDGE_PATH
import trik.testsys.webclient.controller.user.AbstractWebUserMainController
import trik.testsys.webclient.entity.user.impl.Judge
import trik.testsys.webclient.service.entity.user.impl.JudgeService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.view.impl.JudgeView

@Controller
@RequestMapping(JUDGE_PATH)
class JudgeMainController(
    loginData: LoginData
) : AbstractWebUserMainController<Judge, JudgeView, JudgeService>(loginData) {

    override val mainPath = JUDGE_PATH

    override val mainPage = JUDGE_PAGE

    override fun Judge.toView(timeZoneId: String?) = JudgeView(
        id = id,
        name = name,
        accessToken = accessToken,
        creationDate = creationDate?.atTimeZone(timeZoneId),
        lastLoginDate = lastLoginDate?.atTimeZone(timeZoneId),
        additionalInfo = additionalInfo
    )

    companion object {

        const val JUDGE_PATH = "/judge"
        const val JUDGE_PAGE = "judge"
    }
}