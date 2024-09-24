package trik.testsys.webclient.util

import org.springframework.ui.Model

/**
 * @author Roman Shishkin
 * @since 2.0.0
**/
fun Model.addSessionActiveInfo() = addAttribute("hasActiveSession", true)