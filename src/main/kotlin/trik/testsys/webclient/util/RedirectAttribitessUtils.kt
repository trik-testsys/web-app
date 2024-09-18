package trik.testsys.webclient.util

import org.springframework.web.servlet.mvc.support.RedirectAttributes

/**
 * Adds flash attribute which will be used as a popup message.
 *
 * @author Roman Shishkin
 * @since 2.0.0
 */
fun RedirectAttributes.addPopupMessage(message: String?) = addFlashAttribute("message", message)

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
fun RedirectAttributes.addSessionExpiredMessage() = addPopupMessage("Ваша сессия истекла, введите код повторно.")

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
fun RedirectAttributes.addExitMessage() = addPopupMessage("Вы успешно вышли из своего кабинета.")