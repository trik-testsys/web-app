package trik.testsys.webapp.backoffice.controller.menu

import org.springframework.stereotype.Component
import trik.testsys.webapp.backoffice.data.entity.impl.User

data class MenuItem(val name: String, val link: String)
data class MenuSection(val title: String, val items: List<MenuItem>)

@Component
class MenuBuilder {

    fun buildFor(user: User?): List<MenuSection> {
        val sections = mutableListOf<MenuSection>()

        sections += MenuSection(
            title = "Профиль",
            items = listOf(MenuItem(name = "Обзор", link = "/user"))
        )

        if (user?.privileges?.contains(User.Privilege.VIEWER) == true) {
            sections += MenuSection(
                title = "Наблюдатель",
                items = listOf(
                    MenuItem(name = "Организаторы", link = "/user/viewer/admins"),
                    MenuItem(name = "Рег-токен", link = "/user/viewer/token"),
                    MenuItem(name = "Экспорт результатов", link = "/user/viewer/export"),
                )
            )
        }

        // Additional privileges can be added here similarly (ADMIN, DEVELOPER, JUDGE, SUPER_USER)

        return sections
    }
}


