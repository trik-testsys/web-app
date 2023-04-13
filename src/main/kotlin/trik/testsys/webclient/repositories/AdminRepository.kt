package trik.testsys.webclient.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

import trik.testsys.webclient.entities.Admin
import trik.testsys.webclient.entities.WebUser

@Repository
interface AdminRepository: CrudRepository<Admin, String> {

    fun findAdminByWebUser(webUser: WebUser): Admin?

    fun findAdminById(id: Long): Admin?
}