package trik.testsys.webapp.backoffice.controller.impl.user

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webapp.backoffice.controller.AbstractUserController
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.utils.addHasActiveSession
import trik.testsys.webapp.backoffice.utils.addSections
import trik.testsys.webapp.backoffice.utils.addUser
import trik.testsys.webapp.backoffice.utils.PrivilegeI18n

@Controller
@RequestMapping("/user/viewer")
class ViewerController() : AbstractUserController() {

    @GetMapping("/admins")
    fun listAdmins(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val viewer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        val admins = viewer.managedAdmins.sortedBy { it.id }
        setupModel(model, session, viewer)
        model.addAttribute("admins", admins)
        model.addAttribute("privilegeToRu", PrivilegeI18n.asMap())
        return "viewer/admins"
    }

    @GetMapping("/token")
    fun viewTokenRedirect(): String = "redirect:/user/viewer/admins"

    @GetMapping("/export")
    fun exportView(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val viewer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        setupModel(model, session, viewer)
        return "viewer/export"
    }
}