package trik.testsys.webclient.repository.impl

import org.springframework.stereotype.Repository
import trik.testsys.core.repository.user.UserRepository
import trik.testsys.core.utils.marker.TrikRepository

import trik.testsys.webclient.entity.impl.Admin
import trik.testsys.webclient.entity.impl.WebUser

@Repository
interface AdminRepository: UserRepository<Admin>, TrikRepository {

    fun findAdminByWebUser(webUser: WebUser): Admin?
}