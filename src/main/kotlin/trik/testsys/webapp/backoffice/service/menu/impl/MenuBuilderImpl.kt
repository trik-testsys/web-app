package trik.testsys.webapp.backoffice.service.menu.impl

import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.service.menu.MenuBuilder
import trik.testsys.webapp.backoffice.service.menu.section.MenuSectionBuilder
import kotlin.collections.plusAssign

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
class MenuBuilderImpl(
    context: ApplicationContext
) : MenuBuilder {

    private val menuSectionBuilders: Map<User.Privilege, () -> MenuBuilder.MenuSection> by lazy {
        val sectionBuilders = context.getBeansOfType(MenuSectionBuilder::class.java).values.associate {
            it.privilege to it::buildSection
        }

        sectionBuilders
    }

    override fun buildFor(user: User): List<MenuBuilder.MenuSection> {
        val sections = mutableListOf<MenuBuilder.MenuSection>()

        sections += MenuBuilder.MenuSection(
            title = "Профиль",
            items = listOf(MenuBuilder.MenuItem(name = "Обзор", link = "/user"))
        )

        menuSectionBuilders.forEach { (privilege, builder) ->
            if (user.privileges.contains(privilege)) {
                val section = builder.invoke()
                sections += section
            }
        }

        return sections
    }
}