package trik.testsys.webclient.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import trik.testsys.webclient.entities.SuperUser
import trik.testsys.webclient.entities.WebUser
import trik.testsys.webclient.repositories.SuperUserRepository

@Service
class SuperUserService {

    @Autowired
    private lateinit var superUserRepository: SuperUserRepository

    fun getSuperUserByWebUserId(webUserId: Long): SuperUser? {
        return superUserRepository.findSuperUserByWebUserId(webUserId)
    }

    fun getSuperUserByWebUser(webUser: WebUser): SuperUser? {
        return superUserRepository.findSuperUserByWebUser(webUser)
    }
}