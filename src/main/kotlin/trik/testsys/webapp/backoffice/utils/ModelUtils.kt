package trik.testsys.webapp.backoffice.utils

import jakarta.servlet.http.HttpSession
import org.springframework.ui.Model
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.service.menu.MenuBuilder

const val SESSION_ACCESS_TOKEN = "accessToken"
const val HAS_ACTIVE_SESSION = "hasActiveSession"
const val USER = "user"
const val SECTIONS = "menuSections"

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
fun Model.addHasActiveSession(session: HttpSession) {
    val hasActiveSession = session.getAttribute(SESSION_ACCESS_TOKEN) != null
    addAttribute(HAS_ACTIVE_SESSION, hasActiveSession)
}

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
fun Model.addUser(user: User) {
    addAttribute(USER, user)
}

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
fun Model.addSections(sections: Collection<MenuBuilder.MenuSection>) {
    addAttribute(SECTIONS, sections)
}