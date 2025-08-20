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

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    fun addMembers(studentGroup: StudentGroup, members: User)

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    fun removeMember(studentGroup: StudentGroup, member: User): Boolean

    fun findByOwner(owner: User): Set<StudentGroup>

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun create(owner: User, name: String, info: String?): StudentGroup?

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    fun generateStudents(owner: User, group: StudentGroup, count: Int): Set<User>

    fun generateMembersCsv(group: StudentGroup): ByteArray

    fun generateResultsCsv(group: StudentGroup): ByteArray
}