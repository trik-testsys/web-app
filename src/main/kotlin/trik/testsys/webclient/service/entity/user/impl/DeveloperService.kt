package trik.testsys.webclient.service.entity.user.impl

import org.springframework.stereotype.Service
import trik.testsys.webclient.entity.user.impl.Developer
import trik.testsys.webclient.repository.user.DeveloperRepository
import trik.testsys.webclient.service.entity.user.WebUserService

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Service
class DeveloperService : WebUserService<Developer, DeveloperRepository>()

