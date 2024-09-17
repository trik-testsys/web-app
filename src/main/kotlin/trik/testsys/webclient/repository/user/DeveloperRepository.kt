package trik.testsys.webclient.repository.user

import org.springframework.stereotype.Repository
import trik.testsys.core.repository.user.UserRepository
import trik.testsys.core.utils.marker.TrikRepository
import trik.testsys.webclient.entity.impl.user.Developer
import trik.testsys.webclient.entity.impl.user.WebUser

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Repository
interface DeveloperRepository: UserRepository<Developer>, TrikRepository {

    fun findByWebUser(webUser: WebUser): Developer?
}