package trik.testsys.webapp.backoffice.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.ui.Model
import jakarta.servlet.http.HttpSession
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webapp.backoffice.data.service.impl.AccessTokenService
import trik.testsys.webapp.backoffice.data.service.impl.UserServiceImpl
import trik.testsys.webapp.backoffice.service.mapper.impl.UserObjectMapper

@Controller
@RequestMapping("/user")
class UserController(
    private val userObjectMapper: UserObjectMapper,
    private val accessTokenService: AccessTokenService,
    private val userService: UserServiceImpl
) {

    @GetMapping
    fun getUserPage(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = (session.getAttribute("accessToken") as? String)?.let {
            accessTokenService.findByValue(it)
        } ?: run {
            redirectAttributes.addFlashAttribute("message", "Пожалуйста, войдите в систему.")
            return "redirect:/login"
        }

        val userDto = userObjectMapper.toDto(accessToken.user)

        model.addAttribute("hasActiveSession", true)
        model.addAttribute("user", userDto)
        return "user"
    }
}