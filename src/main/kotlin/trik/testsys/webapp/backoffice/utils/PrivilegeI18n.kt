package trik.testsys.webapp.backoffice.utils

import trik.testsys.webapp.backoffice.data.entity.impl.User

/**
 * Centralized i18n for user privileges.
 */
object PrivilegeI18n {

    private val privilegeToRu: Map<User.Privilege, String> = mapOf(
        User.Privilege.ADMIN to "Организатор",
        User.Privilege.DEVELOPER to "Разработчик Задач",
        User.Privilege.JUDGE to "Судья",
        User.Privilege.STUDENT to "Участник",
        User.Privilege.SUPER_USER to "Супервайзер",
        User.Privilege.VIEWER to "Наблюдатель",
        User.Privilege.GROUP_ADMIN to "Администратор Групп",
    )

    @JvmStatic
    fun toRu(privilege: User.Privilege): String = privilegeToRu[privilege] ?: privilege.name

    @JvmStatic
    fun listRu(privileges: Collection<User.Privilege>): List<String> =
        privileges.map { toRu(it) }.sorted()

    @JvmStatic
    fun listOptions(): List<Pair<String, String>> =
        User.Privilege.entries.map { it.name to toRu(it) }

    @JvmStatic
    fun asMap(): Map<User.Privilege, String> = privilegeToRu
}


