package trik.testsys.webapp.backoffice.service.menu.section

import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.service.menu.MenuBuilder

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
interface MenuSectionBuilder {

    val privilege: User.Privilege

    fun buildSection(): MenuBuilder.MenuSection
}