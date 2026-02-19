package trik.testsys.webapp.backoffice.service.menu.section.impl

import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.service.menu.MenuBuilder
import trik.testsys.webapp.backoffice.service.menu.section.MenuSectionBuilder

@Service
class StudentSectionBuilder : MenuSectionBuilder {

    override val privilege = User.Privilege.STUDENT

    override fun buildSection(): MenuBuilder.MenuSection {
        return MenuBuilder.MenuSection(
            title = "Участник",
            items = listOf(
                MenuBuilder.MenuItem(name = "Туры", link = "/user/student/contests"),
            )
        )
    }
}


