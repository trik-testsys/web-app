package trik.testsys.webapp.backoffice.service.menu.section.impl

import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.service.menu.MenuBuilder
import trik.testsys.webapp.backoffice.service.menu.section.MenuSectionBuilder

@Service
class DeveloperSectionBuilder : MenuSectionBuilder {

    override val privilege = User.Privilege.DEVELOPER

    override fun buildSection(): MenuBuilder.MenuSection {
        return MenuBuilder.MenuSection(
            title = "Разработчик",
            items = listOf(
                MenuBuilder.MenuItem(name = "Туры", link = "/user/developer/contests"),
                MenuBuilder.MenuItem(name = "Задачи", link = "/user/developer/tasks"),
                MenuBuilder.MenuItem(name = "Шаблоны Задач", link = "/user/developer/task-templates")
            )
        )
    }
}


