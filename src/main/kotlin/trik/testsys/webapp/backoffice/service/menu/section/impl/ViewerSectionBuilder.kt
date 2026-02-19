package trik.testsys.webapp.backoffice.service.menu.section.impl

import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.service.menu.MenuBuilder
import trik.testsys.webapp.backoffice.service.menu.section.MenuSectionBuilder

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
@Service
class ViewerSectionBuilder : MenuSectionBuilder {

    override val privilege = User.Privilege.VIEWER

    override fun buildSection(): MenuBuilder.MenuSection {
        return MenuBuilder.MenuSection(
            title = "Наблюдатель",
            items = listOf(
                MenuBuilder.MenuItem(name = "Организаторы", link = "/user/viewer/admins"),
                MenuBuilder.MenuItem(name = "Результаты", link = "/user/viewer/export"),
            )
        )
    }
}