package trik.testsys.webclient.repository

import org.springframework.stereotype.Repository
import trik.testsys.core.repository.EntityRepository
import trik.testsys.webclient.entity.user.impl.Admin
import trik.testsys.webclient.entity.impl.Group

@Repository
interface GroupRepository: EntityRepository<Group> {

    fun findGroupsByAdmin(admin: Admin): List<Group>?

    fun findByAccessToken(accessToken: String): Group?
}