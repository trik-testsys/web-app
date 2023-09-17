package trik.testsys.webclient.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

import trik.testsys.webclient.entity.SuperUser
import trik.testsys.webclient.entity.WebUser

@Repository
interface SuperUserRepository: CrudRepository<SuperUser, Long> {

    fun findSuperUserByWebUserId(webUserId: Long): SuperUser?

    fun findSuperUserByWebUser(webUser: WebUser): SuperUser?
}