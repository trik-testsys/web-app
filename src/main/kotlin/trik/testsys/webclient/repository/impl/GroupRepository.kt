package trik.testsys.webclient.repository.impl

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

import trik.testsys.webclient.entity.impl.Admin
import trik.testsys.webclient.entity.impl.Group

@Repository
interface GroupRepository: CrudRepository<Group, Long> {

    fun findGroupById(id: Long): Group?

    fun findGroupsByAdmin(admin: Admin): List<Group>?

    fun findGroupByAccessToken(accessToken: String): Group?
}