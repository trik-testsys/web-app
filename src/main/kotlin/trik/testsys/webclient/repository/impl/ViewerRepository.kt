package trik.testsys.webclient.repository.impl

import org.springframework.stereotype.Repository
import trik.testsys.core.repository.user.UserRepository
import trik.testsys.core.utils.marker.TrikRepository
import trik.testsys.webclient.entity.impl.Viewer
import trik.testsys.webclient.entity.impl.WebUser

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Repository
interface ViewerRepository : UserRepository<Viewer>, TrikRepository {

    fun findByWebUser(webUser: WebUser): Viewer?

    fun findByAdminRegToken(adminRegToken: String): Viewer?
}