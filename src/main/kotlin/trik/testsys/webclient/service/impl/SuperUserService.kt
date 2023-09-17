package trik.testsys.webclient.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import trik.testsys.webclient.entity.SuperUser
import trik.testsys.webclient.entity.WebUser
import trik.testsys.webclient.repository.SuperUserRepository
import trik.testsys.webclient.service.TrikService

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