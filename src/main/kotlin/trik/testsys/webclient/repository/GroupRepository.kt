package trik.testsys.webclient.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

import trik.testsys.webclient.entity.impl.user.Admin
import trik.testsys.webclient.entity.impl.Group

@Repository
interface GroupRepository: CrudRepository<Group, Long> {

    fun findGroupById(id: Long): Group?

    fun findGroupsByAdmin(admin: Admin): List<Group>?

    fun findGroupByAccessToken(accessToken: String): Group?
}