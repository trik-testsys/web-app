package trik.testsys.webapp.backoffice.data.service

import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import trik.testsys.webapp.backoffice.data.entity.impl.StudentGroup
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.core.data.service.EntityService

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
interface StudentGroupService : EntityService<StudentGroup> {

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    fun addMember(studentGroup: StudentGroup, member: User): Boolean

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    fun removeMember(studentGroup: StudentGroup, member: User): Boolean

    fun findByOwner(owner: User): Set<StudentGroup>

    fun create(owner: User, name: String, info: String?): StudentGroup?
}