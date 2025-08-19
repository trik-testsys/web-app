package trik.testsys.webapp.backoffice.service.menu.section.impl

import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.service.menu.MenuBuilder
import trik.testsys.webapp.backoffice.service.menu.section.MenuSectionBuilder

/**
 * Builds menu section for users with SUPER_USER privilege.
 */
@Service
class SuperUserSectionBuilder : MenuSectionBuilder {

    override val privilege = User.Privilege.SUPER_USER

    override fun buildSection(): MenuBuilder.MenuSection {
        return MenuBuilder.MenuSection(
            title = "Супервайзер",
            items = listOf(
                MenuBuilder.MenuItem(name = "Пользователи", link = "/user/superuser/users"),
            )
        )
    }
}


