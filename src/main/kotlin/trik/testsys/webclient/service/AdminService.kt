package trik.testsys.webclient.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import trik.testsys.webclient.entity.Admin
import trik.testsys.webclient.entity.WebUser
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

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    fun getAllByIds(ids: List<Long>): List<Admin> {
        return adminRepository.findAllById(ids).toList()
    }

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    fun getByAccessToken(accessToken: String): Admin? {
        val webUser = webUserRepository.findWebUserByAccessToken(accessToken) ?: return null
        return adminRepository.findAdminByWebUser(webUser)
    }

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    fun getAll(): List<Admin> {
        return adminRepository.findAll().toList()
    }
}