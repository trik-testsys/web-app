package trik.testsys.webapp.backoffice.data.service

import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import trik.testsys.webapp.backoffice.data.entity.impl.TaskTemplate
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.core.data.service.EntityService

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
interface TaskTemplateService : EntityService<TaskTemplate> {

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    fun findByDeveloper(developer: User): Set<TaskTemplate>
}