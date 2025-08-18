package trik.testsys.webapp.backoffice.data.service

import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.entity.impl.UserGroup

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
interface UserGroupService {

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    fun addMember(userGroup: UserGroup, user: User): Boolean

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    fun removeMember(userGroup: UserGroup, user: User): Boolean
}