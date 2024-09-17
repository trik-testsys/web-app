package trik.testsys.webclient.service.impl.user

import org.springframework.stereotype.Service
import trik.testsys.core.service.user.AbstractUserService
import trik.testsys.core.utils.marker.TrikService
import trik.testsys.webclient.entity.impl.user.WebUser
import trik.testsys.webclient.repository.user.WebUserRepository

/**
 * @author Roman Shishkin
 * @since 1.0.0
 */
@Service
class WebUserService : AbstractUserService<WebUser, WebUserRepository>(), TrikService