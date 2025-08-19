package trik.testsys.webapp.backoffice.utils

import org.springframework.web.servlet.mvc.support.RedirectAttributes

const val MESSAGE = "message"

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
fun RedirectAttributes.addMessage(message: String) = addFlashAttribute(MESSAGE, message)