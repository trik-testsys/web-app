package trik.testsys.webapp.backoffice.data.repository

import org.springframework.stereotype.Repository
import trik.testsys.webapp.backoffice.data.entity.impl.Contest
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.entity.impl.UserGroup
import trik.testsys.webapp.core.data.repository.EntityRepository

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 **/
@Repository
interface ContestRepository : EntityRepository<Contest> {

    fun findByDeveloper(developer: User): Set<Contest>

    fun findDistinctByUserGroupsIn(userGroups: Collection<UserGroup>): Set<Contest>
}