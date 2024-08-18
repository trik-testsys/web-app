package trik.testsys.webclient.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

import trik.testsys.webclient.entity.impl.SuperUser
import trik.testsys.webclient.entity.impl.WebUser

@Repository
interface SuperUserRepository: CrudRepository<SuperUser, String> {

    fun findSuperUserByWebUserId(webUserId: Long): SuperUser?

    fun findSuperUserByWebUser(webUser: WebUser): SuperUser?
}