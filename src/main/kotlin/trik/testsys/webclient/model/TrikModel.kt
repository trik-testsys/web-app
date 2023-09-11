package trik.testsys.webclient.model

import org.springframework.web.servlet.ModelAndView

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
interface TrikModel {

    /**
     * Converts model to map. For [ModelAndView.model] usage.
     */
    fun asMap(): Map<String, Any?>
}