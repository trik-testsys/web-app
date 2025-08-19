package trik.testsys.webapp.backoffice.service.menu.section.impl

import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.service.menu.MenuBuilder
import trik.testsys.webapp.backoffice.service.menu.section.MenuSectionBuilder

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
class AdminSectionBuilder : MenuSectionBuilder {

    override val privilege = User.Privilege.ADMIN

    override fun buildSection(): MenuBuilder.MenuSection {
        return MenuBuilder.MenuSection(
            title = "Организатор",
            items = listOf(
                MenuBuilder.MenuItem(name = "Группы", link = "/user/admin/groups"),
                MenuBuilder.MenuItem(name = "Создать группу", link = "/user/admin/groups/create")
            )
        )
    }
}