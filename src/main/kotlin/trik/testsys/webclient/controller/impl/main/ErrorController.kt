package trik.testsys.webclient.controller.impl.main

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import javax.servlet.http.HttpServletRequest

@Controller
class ErrorController : ErrorController {

    @RequestMapping(ERROR_PATH)
    fun handleError(request: HttpServletRequest, model: Model): String {
        val statusCode = request.getAttribute("javax.servlet.error.status_code") as? Int

        if (statusCode != null) {
            if (statusCode % 100 == 5) {
                model.addAttribute("message", "Произошла ошибка на сервере. Пожалуйста, попробуйте позже")
            } else if (statusCode % 100 == 4) {
                model.addAttribute("message", "Запрашиваемая страница не найдена")
            }
        }
        return ERROR_PAGE
    }

    companion object {

        const val ERROR_PATH = "/error"
        const val ERROR_PAGE = "error"
    }
}