package trik.testsys.webapp.backoffice.data.service

import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import trik.testsys.webapp.backoffice.data.entity.impl.User

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
interface SuperUserService {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun createUser(superUser: User, name: String, privileges: Collection<User.Privilege>): Boolean

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun addPrivilege(superUser: User, user: User, privilege: User.Privilege): Boolean

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun addPrivileges(superUser: User, user: User, privileges: Collection<User.Privilege>): Boolean =
        privileges.forEach { addPrivilege(superUser, user, it) }.let { true }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    fun findAllSuperUser(isAllUserSuperUser: Boolean? = null): Set<User>

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun removeUser(superUser: User, user: User): Boolean
}