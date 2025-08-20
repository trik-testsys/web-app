package trik.testsys.webapp.backoffice.data.service.impl;

import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.Contest
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.repository.ContestRepository
import trik.testsys.webapp.backoffice.data.service.ContestService
import trik.testsys.webapp.core.data.service.AbstractService

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
class ContestServiceImpl(

) :
    AbstractService<Contest, ContestRepository>(),
    ContestService {

    override fun findForUser(user: User): Set<Contest> {
        val memberedGroups = user.memberedGroups
        val sharedContests = memberedGroups.flatMap { it.contests }.toSet()
        return sharedContests
        val sharedWithGroups = if (user.memberedGroups.isEmpty()) emptySet() else repository.findByUserGroupsContaining(user.memberedGroups)
    }
}