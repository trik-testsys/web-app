package trik.testsys.webapp.backoffice.data.service.impl

import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.TaskTemplate
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.entity.impl.UserGroup
import trik.testsys.webapp.backoffice.data.repository.TaskTemplateRepository
import trik.testsys.webapp.backoffice.data.service.TaskTemplateService
import trik.testsys.webapp.core.data.service.AbstractService

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
class TaskTemplateServiceImpl :
    AbstractService<TaskTemplate, TaskTemplateRepository>(),
    TaskTemplateService {

    override fun findByDeveloper(developer: User): Set<TaskTemplate> {
        return repository.findByDeveloper(developer)
    }

    override fun findForUserGroups(userGroups: Collection<UserGroup>): Set<TaskTemplate> {
        return repository.findDistinctByUserGroupsIn(userGroups)
    }
}