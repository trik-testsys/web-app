package trik.testsys.webapp.backoffice.controller.impl.user.viewer

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.controller.menu.MenuBuilder
import trik.testsys.webapp.backoffice.data.service.impl.AccessTokenService

@Controller
@RequestMapping("/user/viewer")
class ViewerController(
    private val accessTokenService: AccessTokenService,
    private val menuBuilder: MenuBuilder
) {

    @GetMapping("/admins")
    fun listAdmins(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val token = (session.getAttribute("accessToken") as? String)?.let { accessTokenService.findByValue(it) }
            ?: run {
                redirectAttributes.addFlashAttribute("message", "Пожалуйста, войдите в систему.")
                return "redirect:/login"
            }

        val viewer = token.user ?: run {
            redirectAttributes.addFlashAttribute("message", "Пользователь не найден.")
            return "redirect:/login"
        }

        val admins = viewer.managedAdmins.sortedBy { it.id }

        val sections = menuBuilder.buildFor(viewer)
        model.addAttribute("hasActiveSession", true)
        model.addAttribute("user", viewer)
        model.addAttribute("menuSections", sections)
        model.addAttribute("admins", admins)
        return "viewer/admins"
    }

    @GetMapping("/token")
    fun viewToken(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val token = (session.getAttribute("accessToken") as? String)?.let { accessTokenService.findByValue(it) }
            ?: run {
                redirectAttributes.addFlashAttribute("message", "Пожалуйста, войдите в систему.")
                return "redirect:/login"
            }

        val viewer = token.user ?: run {
            redirectAttributes.addFlashAttribute("message", "Пользователь не найден.")
            return "redirect:/login"
        }

        val sections = menuBuilder.buildFor(viewer)
        model.addAttribute("hasActiveSession", true)
        model.addAttribute("user", viewer)
        model.addAttribute("menuSections", sections)
        return "viewer/token"
    }

    @GetMapping("/export")
    fun exportView(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val token = (session.getAttribute("accessToken") as? String)?.let { accessTokenService.findByValue(it) }
            ?: run {
                redirectAttributes.addFlashAttribute("message", "Пожалуйста, войдите в систему.")
                return "redirect:/login"
            }

        val viewer = token.user ?: run {
            redirectAttributes.addFlashAttribute("message", "Пользователь не найден.")
            return "redirect:/login"
        }

        val sections = menuBuilder.buildFor(viewer)
        model.addAttribute("hasActiveSession", true)
        model.addAttribute("user", viewer)
        model.addAttribute("menuSections", sections)
        return "viewer/export"
    }

    // sections are built by MenuBuilder
}


