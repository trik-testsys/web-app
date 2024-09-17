package trik.testsys.webclient.service.impl.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import trik.testsys.webclient.entity.impl.user.SuperUser
import trik.testsys.webclient.entity.impl.user.WebUser
import trik.testsys.webclient.repository.user.SuperUserRepository

@Service
class SuperUserService @Autowired constructor(
    private val superUserRepository: SuperUserRepository
) : TrikService {

    fun getSuperUserByWebUserId(webUserId: Long): SuperUser? {
        return superUserRepository.findSuperUserByWebUserId(webUserId)
    }

    fun getSuperUserByWebUser(webUser: WebUser): SuperUser? {
        return superUserRepository.findSuperUserByWebUser(webUser)
    }
}