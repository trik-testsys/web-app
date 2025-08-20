package trik.testsys.webapp.backoffice.data.repository;

import org.springframework.stereotype.Repository
import trik.testsys.webapp.backoffice.data.entity.impl.TaskTemplate
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.core.data.repository.EntityRepository

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Repository
interface TaskTemplateRepository : EntityRepository<TaskTemplate> {

    fun findByDeveloper(developer: User): Set<TaskTemplate>
}