package trik.testsys.webclient.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

import trik.testsys.webclient.entity.impl.Admin
import trik.testsys.webclient.entity.impl.WebUser

@Repository
interface AdminRepository: CrudRepository<Admin, String> {

    fun findAdminByWebUser(webUser: WebUser): Admin?

    fun findAdminById(id: Long): Admin?
}