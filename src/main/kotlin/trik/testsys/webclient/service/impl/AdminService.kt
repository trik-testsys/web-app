package trik.testsys.webclient.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import trik.testsys.webclient.entity.Admin
import trik.testsys.webclient.entity.Viewer
import trik.testsys.webclient.entity.WebUser
import trik.testsys.webclient.repository.AdminRepository
import trik.testsys.webclient.repository.StudentRepository
import trik.testsys.webclient.repository.WebUserRepository
import trik.testsys.webclient.service.TrikService

@Service
class AdminService @Autowired constructor(
    private val adminRepository: AdminRepository,
    private val webUserRepository: WebUserRepository,
    private val studentRepository: StudentRepository
) : TrikService {

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

    fun saveAll(admins: Collection<Admin>): List<Admin> {
        return adminRepository.saveAll(admins).toList()
    }

    fun getAdminByWebUserId(webUserId: Long): Admin? {
        val webUser = webUserRepository.findWebUserById(webUserId) ?: return null
        return adminRepository.findAdminByWebUser(webUser)
    }

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    fun save(admin: Admin): Admin {
        return adminRepository.save(admin)
    }

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    fun save(webUser: WebUser, viewer: Viewer): Admin {
        val admin = Admin(webUser, viewer)
        return adminRepository.save(admin)
    }

    fun getAdminByWebUser(webUser: WebUser): Admin? {
        return adminRepository.findByWebUser(webUser)
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
    fun getById(id: Long): Admin? {
        return adminRepository.findAdminById(id)
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