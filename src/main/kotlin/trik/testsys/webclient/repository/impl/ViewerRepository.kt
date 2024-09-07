package trik.testsys.webclient.repository.impl

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import trik.testsys.webclient.entity.impl.Viewer
import trik.testsys.webclient.entity.impl.WebUser

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Repository
interface ViewerRepository : CrudRepository<Viewer, Long> {

    fun findViewerByWebUser(webUser: WebUser): Viewer?

    fun findByAdminRegToken(adminRegToken: String): Viewer?
}