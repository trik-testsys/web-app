package trik.testsys.webclient.util

import org.springframework.ui.Model

/**
 * Adds `hasActiveSession` with value `true` attribute to model which indicates that user has active session.
 *
 * @author Roman Shishkin
 * @since 2.0.0
**/
fun Model.addSessionActiveInfo() = addAttribute("hasActiveSession", true)