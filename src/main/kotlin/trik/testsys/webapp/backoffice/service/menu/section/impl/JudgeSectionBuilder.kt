package trik.testsys.webapp.backoffice.service.menu.section.impl

import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.service.menu.MenuBuilder
import trik.testsys.webapp.backoffice.service.menu.section.MenuSectionBuilder

@Service
class JudgeSectionBuilder : MenuSectionBuilder {

    override val privilege = User.Privilege.JUDGE

    override fun buildSection(): MenuBuilder.MenuSection {
        return MenuBuilder.MenuSection(
            title = "Судья",
            items = listOf(
                MenuBuilder.MenuItem(name = "Посылки", link = "/user/judge/solutions"),
            )
        )
    }
}


