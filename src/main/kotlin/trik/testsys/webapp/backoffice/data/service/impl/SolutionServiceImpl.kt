package trik.testsys.webapp.backoffice.data.service.impl

import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.repository.SolutionRepository
import trik.testsys.webapp.backoffice.data.service.SolutionService
import trik.testsys.webapp.core.data.service.AbstractService

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
class SolutionServiceImpl :
    AbstractService<Solution, SolutionRepository>(),
    SolutionService {

}