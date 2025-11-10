package trik.testsys.webapp.backoffice.data.service

import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.entity.impl.UserGroup
import trik.testsys.webapp.core.data.service.EntityService
import java.time.Instant

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
interface UserService : EntityService<User> {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun updateName(user: User, newName: String): User

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun updateEmail(user: User, newEmail: String?): User

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun updateLastLoginAt(user: User, lastLoginAt: Instant? = null): User

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    fun findAllGroupAdmin(): Set<User>

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    fun findAllBySuperUser(superUser: User): Set<User>

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun createUserByGroupAdmin(
        groupAdmin: User,
        name: String,
        privileges: Collection<User.Privilege>
    ): User?

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    fun findCandidatesFor(userGroup: UserGroup): Set<User>

    fun findByEmail(email: String): User?
}