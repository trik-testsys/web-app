package trik.testsys.webclient.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

import trik.testsys.webclient.entities.SuperUser
import trik.testsys.webclient.entities.WebUser

@Repository
interface SuperUserRepository: CrudRepository<SuperUser, String> {

    fun findSuperUserByWebUserId(webUserId: Long): SuperUser?

    fun findSuperUserByWebUser(webUser: WebUser): SuperUser?
}