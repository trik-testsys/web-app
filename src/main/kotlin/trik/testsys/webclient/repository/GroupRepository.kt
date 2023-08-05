package trik.testsys.webclient.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

import trik.testsys.webclient.entity.Admin
import trik.testsys.webclient.entity.Group

@Repository
interface GroupRepository: CrudRepository<Group, String> {

    fun findGroupById(id: Long): Group?

    fun findGroupsByAdmin(admin: Admin): List<Group>?

    fun findGroupByAccessToken(accessToken: String): Group?
}