package trik.testsys.webclient.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import trik.testsys.webclient.entities.Admin
import trik.testsys.webclient.entities.WebUser
import trik.testsys.webclient.repositories.AdminRepository
import trik.testsys.webclient.repositories.StudentRepository
import trik.testsys.webclient.repositories.WebUserRepository

@Service
class AdminService {

    @Autowired
    private lateinit var adminRepository: AdminRepository

    @Autowired
    private lateinit var webUserRepository: WebUserRepository

    @Autowired
    private lateinit var studentRepository: StudentRepository

    fun saveAdmin(webUserId: Long): Admin? {
        val webUser = webUserRepository.findWebUserById(webUserId) ?: return null
        if (adminRepository.findAdminByWebUser(webUser) != null) return null
        if (studentRepository.findStudentByWebUser(webUser) != null) return null

        val admin = Admin(webUser)
        return adminRepository.save(admin)
    }

    fun saveAdmin(webUser: WebUser): Admin? {
        if (adminRepository.findAdminByWebUser(webUser) != null) return null
        if (studentRepository.findStudentByWebUser(webUser) != null) return null

        val admin = Admin(webUser)
        return adminRepository.save(admin)
    }

    fun getAdminByWebUserId(webUserId: Long): Admin? {
        val webUser = webUserRepository.findWebUserById(webUserId) ?: return null
        return adminRepository.findAdminByWebUser(webUser)
    }

    fun getAdminByWebUser(webUser: WebUser): Admin? {
        return adminRepository.findAdminByWebUser(webUser)
    }
}