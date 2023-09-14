package trik.testsys.webclient.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import trik.testsys.webclient.entity.Viewer
import trik.testsys.webclient.entity.WebUser

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Repository
interface ViewerRepository : CrudRepository<Viewer, Long> {

    fun findViewerByWebUser(webUser: WebUser): Viewer?
}