package trik.testsys.webapp.backoffice.utils

import org.springframework.web.servlet.mvc.support.RedirectAttributes

const val MESSAGE = "message"

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
fun RedirectAttributes.addMessage(message: String) = addFlashAttribute(MESSAGE, message)