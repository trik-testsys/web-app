package trik.testsys.webclient.service.impl.user

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import trik.testsys.core.service.user.AbstractUserService
import trik.testsys.core.utils.marker.TrikService
import trik.testsys.webclient.entity.impl.user.Admin
import trik.testsys.webclient.entity.impl.user.Viewer
import trik.testsys.webclient.entity.impl.user.WebUser
import trik.testsys.webclient.repository.user.AdminRepository
import trik.testsys.webclient.repository.user.StudentRepository
import trik.testsys.webclient.repository.user.WebUserRepository

@Service
class AdminService(
    private val webUserRepository: WebUserRepository,
    private val studentRepository: StudentRepository
) : AbstractUserService<Admin, AdminRepository>(), TrikService {

    fun saveAdmin(webUserId: Long): Admin? {
        val webUser = webUserRepository.findByIdOrNull(webUserId) ?: return null
        if (repository.findAdminByWebUser(webUser) != null) return null
        if (studentRepository.findByWebUser(webUser) != null) return null

        val admin = Admin(webUser)
        return repository.save(admin)
    }

    fun saveAdmin(webUser: WebUser): Admin? {
        if (repository.findAdminByWebUser(webUser) != null) return null
        if (studentRepository.findByWebUser(webUser) != null) return null

        val admin = Admin(webUser)
        return repository.save(admin)
    }

    fun saveAll(admins: Collection<Admin>): List<Admin> {
        return repository.saveAll(admins).toList()
    }

    fun getAdminByWebUserId(webUserId: Long): Admin? {
        val webUser = webUserRepository.findByIdOrNull(webUserId) ?: return null
        return repository.findAdminByWebUser(webUser)
    }

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    fun save(webUser: WebUser, viewer: Viewer): Admin {
        val admin = Admin(webUser, viewer)
        return repository.save(admin)
    }

    fun getAdminByWebUser(webUser: WebUser): Admin? {
        return repository.findAdminByWebUser(webUser)
    }

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    fun getAllByIds(ids: List<Long>): List<Admin> {
        return repository.findAllById(ids).toList()
    }

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    fun getById(id: Long): Admin? {
        return repository.findByIdOrNull(id)
    }
    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    fun getByAccessToken(accessToken: String): Admin? {
        val webUser = webUserRepository.findByAccessToken(accessToken) ?: return null
        return repository.findAdminByWebUser(webUser)
    }

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    fun getAll(): List<Admin> {
        return repository.findAll().toList()
    }
}