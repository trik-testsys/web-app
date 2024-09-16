package trik.testsys.webclient.service.impl

import org.springframework.stereotype.Service
import trik.testsys.core.service.user.AbstractUserService
import trik.testsys.core.utils.marker.TrikService

import trik.testsys.webclient.entity.impl.WebUser
import trik.testsys.webclient.repository.impl.WebUserRepository

/**
 * @author Roman Shishkin
 * @since 1.0.0
 */
@Service
class WebUserService : AbstractUserService<WebUser, WebUserRepository>(), TrikService