package trik.testsys.webapp.backoffice.data.service

import trik.testsys.webapp.backoffice.data.entity.impl.User

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
interface SuperUserService {

    fun createUser(superUser: User, name: String, privileges: Collection<User.Privilege>): Boolean

    fun addPrivilege(superUser: User, user: User, privilege: User.Privilege): Boolean

    fun addPrivileges(superUser: User, user: User, privileges: Collection<User.Privilege>): Boolean =
        privileges.forEach { addPrivilege(superUser, user, it) }.let { true }
}