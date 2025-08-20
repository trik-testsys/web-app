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
class GroupAdminSectionBuilder : MenuSectionBuilder {

    override val privilege = User.Privilege.GROUP_ADMIN

    override fun buildSection(): MenuBuilder.MenuSection {
        return MenuBuilder.MenuSection(
            title = "Администратор Групп",
            items = listOf(
                MenuBuilder.MenuItem(name = "Группы Пользователей", link = "/user/group-admin/groups"),
            )
        )
    }
}


