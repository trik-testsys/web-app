package trik.testsys.webclient.controller.impl.main

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.security.login.LoginData
import trik.testsys.webclient.service.impl.user.*

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
@Controller
@RequestMapping(RedirectController.REDIRECT_PATH)
class RedirectController(
    private val loginData: LoginData,

    private val superUserService: SuperUserService,
    private val adminService: AdminService,
    private val studentService: StudentService,
    private val developerService: DeveloperService,
    private val viewerService: ViewerService,
    private val judgeService: JudgeService,
) {

    @GetMapping
    fun redirectGet(redirectAttributes: RedirectAttributes): String {
        val webUser = loginData.webUser ?: run {
            redirectAttributes.addFlashAttribute("message", "Истекла сессия")
            return "redirect:${LoginController.LOGIN_PATH}"
        }

        studentService.getByWebUser(webUser)?.let { return "redirect:/student" }
        adminService.getAdminByWebUser(webUser)?.let { return "redirect:/admin" }
        viewerService.getByWebUser(webUser)?.let { return "redirect:/viewer" }
        developerService.getByWebUser(webUser)?.let { return "redirect:/developer" }
        judgeService.getByWebUser(webUser)?.let { return "redirect:/judge" }
        superUserService.getSuperUserByWebUser(webUser)?.let { return "redirect:/superuser" }

        redirectAttributes.addFlashAttribute("message", "Некорретный Код-доступа. Попробуйте еще раз.")
        return "redirect:${LoginController.LOGIN_PATH}"
    }

    companion object {

        internal const val REDIRECT_PATH = "/redirect"
    }
}