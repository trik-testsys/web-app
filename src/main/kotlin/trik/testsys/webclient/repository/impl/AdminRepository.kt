package trik.testsys.webclient.repository.impl

import org.springframework.stereotype.Repository

import trik.testsys.webclient.entity.impl.Admin
import trik.testsys.webclient.entity.impl.WebUser
import trik.testsys.webclient.repository.TrikRepository

@Repository
interface AdminRepository: TrikRepository<Admin> {

    fun findAdminByWebUser(webUser: WebUser): Admin?

    fun findAdminById(id: Long): Admin?
}