package trik.testsys.webclient.service.impl

import org.springframework.stereotype.Service
import trik.testsys.core.service.user.AbstractUserService
import trik.testsys.core.utils.marker.TrikService
import trik.testsys.webclient.entity.impl.Developer
import trik.testsys.webclient.entity.impl.WebUser

import trik.testsys.webclient.repository.impl.DeveloperRepository

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Service
class DeveloperService : AbstractUserService<Developer, DeveloperRepository>(), TrikService {

    fun getByWebUser(webUser: WebUser): Developer? {
        return repository.findByWebUser(webUser)
    }
}

