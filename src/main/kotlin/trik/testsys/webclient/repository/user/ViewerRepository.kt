package trik.testsys.webclient.repository.user

import org.springframework.stereotype.Repository
import trik.testsys.core.repository.user.UserRepository
import trik.testsys.webclient.entity.user.impl.Viewer

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Repository
interface ViewerRepository : UserRepository<Viewer> {

    fun findByAdminRegToken(adminRegToken: String): Viewer?
}