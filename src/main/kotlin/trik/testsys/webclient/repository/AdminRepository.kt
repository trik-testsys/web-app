package trik.testsys.webclient.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

import trik.testsys.webclient.entity.Admin
import trik.testsys.webclient.entity.WebUser

@Repository
interface AdminRepository: CrudRepository<Admin, Long> {

    fun findAdminByWebUser(webUser: WebUser): Admin?

    fun findAdminById(id: Long): Admin?
}