package trik.testsys.webclient.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

import trik.testsys.webclient.entities.Admin
import trik.testsys.webclient.entities.Group

@Repository
interface GroupRepository: CrudRepository<Group, String> {

    fun findGroupById(id: Long): Group?

    fun findGroupByName(name: String): Group?

    fun findGroupsByAdmin(admin: Admin): List<Group>?

    fun findGroupByAccessToken(accessToken: String): Group?
}