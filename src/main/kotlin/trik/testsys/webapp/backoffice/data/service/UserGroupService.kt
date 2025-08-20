package trik.testsys.webapp.backoffice.data.service

import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.entity.impl.UserGroup
import trik.testsys.webapp.core.data.service.EntityService

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
interface UserGroupService : EntityService<UserGroup> {

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    fun addMember(userGroup: UserGroup, user: User): Boolean

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    fun removeMember(userGroup: UserGroup, user: User): Boolean

    fun findByOwner(owner: User): Set<UserGroup>

    fun findByMember(member: User): Set<UserGroup>

    @Transactional(propagation = Propagation.REQUIRED)
    fun create(owner: User, name: String, info: String?): UserGroup?

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    fun getOrCreateDefaultGroup(owner: User): UserGroup

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    fun getDefaultGroup(): UserGroup?
}