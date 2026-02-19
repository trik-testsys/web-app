package trik.testsys.webapp.backoffice.service.menu

import trik.testsys.webapp.backoffice.data.entity.impl.User

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
interface MenuBuilder {

    fun buildFor(user: User): List<MenuSection>

    data class MenuSection(val title: String, val items: List<MenuItem>)

    data class MenuItem(val name: String, val link: String)
}