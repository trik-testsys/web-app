package trik.testsys.webclient.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import trik.testsys.webclient.entity.impl.Viewer
import trik.testsys.webclient.entity.impl.WebUser
import trik.testsys.webclient.repository.ViewerRepository

@Service
class ViewerService @Autowired constructor(
    private val viewerRepository: ViewerRepository
) {

    fun getByWebUser(webUser: WebUser): Viewer? {
        return viewerRepository.findViewerByWebUser(webUser)
    }
}