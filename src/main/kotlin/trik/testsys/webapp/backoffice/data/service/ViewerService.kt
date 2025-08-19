package trik.testsys.webapp.backoffice.data.service

import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import trik.testsys.webapp.backoffice.data.entity.impl.RegToken
import trik.testsys.webapp.backoffice.data.entity.impl.User

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
interface ViewerService {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun createAdmin(regToken: RegToken, name: String?): User?
}