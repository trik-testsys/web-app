package trik.testsys.webclient.service.entity.user.impl

import org.springframework.stereotype.Service
import trik.testsys.core.utils.marker.TrikService
import trik.testsys.webclient.entity.user.impl.Viewer
import trik.testsys.webclient.repository.user.ViewerRepository
import trik.testsys.webclient.service.entity.user.WebUserService

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Service
class ViewerService : WebUserService<Viewer, ViewerRepository>(), TrikService {

    fun getByAdminRegToken(adminRegToken: String): Viewer? {
        return repository.findByAdminRegToken(adminRegToken)
    }
}