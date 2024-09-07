package trik.testsys.webclient.repository.impl

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

import trik.testsys.webclient.entity.impl.SuperUser
import trik.testsys.webclient.entity.impl.WebUser

@Repository
interface SuperUserRepository: CrudRepository<SuperUser, Long> {

    fun findSuperUserByWebUserId(webUserId: Long): SuperUser?

    fun findSuperUserByWebUser(webUser: WebUser): SuperUser?
}