package trik.testsys.webclient.repository.user

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import trik.testsys.webclient.entity.impl.user.SuperUser
import trik.testsys.webclient.entity.impl.user.WebUser

@Repository
interface SuperUserRepository: CrudRepository<SuperUser, Long> {

    fun findSuperUserByWebUserId(webUserId: Long): SuperUser?

    fun findSuperUserByWebUser(webUser: WebUser): SuperUser?
}