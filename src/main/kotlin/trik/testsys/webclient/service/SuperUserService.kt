package trik.testsys.webclient.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import trik.testsys.webclient.entity.impl.SuperUser
import trik.testsys.webclient.entity.impl.WebUser
import trik.testsys.webclient.repository.SuperUserRepository

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