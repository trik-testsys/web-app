package trik.testsys.webclient.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import trik.testsys.webclient.entity.impl.Admin
import trik.testsys.webclient.entity.impl.WebUser
import trik.testsys.webclient.repository.AdminRepository
import trik.testsys.webclient.repository.StudentRepository
import trik.testsys.webclient.repository.WebUserRepository

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
        if (studentRepository.findByWebUser(webUser) != null) return null

        val admin = Admin(webUser)
        return adminRepository.save(admin)
    }

    fun saveAdmin(webUser: WebUser): Admin? {
        if (adminRepository.findAdminByWebUser(webUser) != null) return null
        if (studentRepository.findByWebUser(webUser) != null) return null

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