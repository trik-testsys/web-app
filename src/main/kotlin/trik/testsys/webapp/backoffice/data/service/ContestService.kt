package trik.testsys.webapp.backoffice.data.service

import trik.testsys.webapp.backoffice.data.entity.impl.Contest
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.core.data.service.EntityService

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
interface ContestService : EntityService<Contest> {

    fun findForUser(user: User): Set<Contest>
}