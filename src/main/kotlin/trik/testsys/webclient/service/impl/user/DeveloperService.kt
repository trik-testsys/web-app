package trik.testsys.webclient.service.impl.user

import org.springframework.stereotype.Service
import trik.testsys.core.service.user.AbstractUserService
import trik.testsys.core.utils.marker.TrikService
import trik.testsys.webclient.entity.impl.user.Developer
import trik.testsys.webclient.repository.user.DeveloperRepository

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Service
class DeveloperService : AbstractUserService<Developer, DeveloperRepository>(), TrikService

