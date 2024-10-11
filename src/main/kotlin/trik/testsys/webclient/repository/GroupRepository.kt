package trik.testsys.webclient.repository

import org.springframework.stereotype.Repository
import trik.testsys.webclient.entity.impl.Contest
import trik.testsys.webclient.entity.user.impl.Admin
import trik.testsys.webclient.entity.impl.Group

@Repository
interface GroupRepository : RegEntityRepository<Group> {

    fun findGroupsByAdmin(admin: Admin): List<Group>?
}