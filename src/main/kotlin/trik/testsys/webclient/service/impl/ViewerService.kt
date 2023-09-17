package trik.testsys.webclient.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import trik.testsys.webclient.entity.Viewer
import trik.testsys.webclient.entity.WebUser
import trik.testsys.webclient.repository.ViewerRepository
import trik.testsys.webclient.service.TrikService

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Service
class ViewerService @Autowired constructor(
    private val viewerRepository: ViewerRepository
) : TrikService {

    fun getByWebUser(webUser: WebUser): Viewer? {
        return viewerRepository.findViewerByWebUser(webUser)
    }

    fun save(viewer: Viewer): Viewer {
        return viewerRepository.save(viewer)
    }
}