package trik.testsys.webapp.backoffice.data.repository

import org.springframework.stereotype.Repository
import trik.testsys.webapp.backoffice.data.entity.impl.StudentGroup
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.core.data.repository.EntityRepository

@Repository
interface StudentGroupRepository : EntityRepository<StudentGroup> {

    fun findByOwner(owner: User): Set<StudentGroup>

    fun existsByContests_Id(contestId: Long): Boolean
}