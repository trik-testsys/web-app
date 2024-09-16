package trik.testsys.webclient.repository.impl

import org.springframework.stereotype.Repository
import trik.testsys.webclient.entity.impl.WebUser
import trik.testsys.core.repository.user.UserRepository
import trik.testsys.core.utils.marker.TrikRepository

/**
 * @author Roman Shishkin
 * @since 1.0.0
 */
@Repository
interface WebUserRepository : UserRepository<WebUser>, TrikRepository