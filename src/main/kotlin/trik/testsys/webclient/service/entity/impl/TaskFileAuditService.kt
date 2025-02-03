package trik.testsys.webclient.service.entity.impl

import org.springframework.stereotype.Service
import trik.testsys.core.service.AbstractService
import trik.testsys.webclient.entity.impl.TaskFileAudit
import trik.testsys.webclient.repository.TaskFileAuditRepository

/**
 * @author Roman Shishkin
 * @since 2.1.0
 */
@Service
class TaskFileAuditService : AbstractService<TaskFileAudit, TaskFileAuditRepository>() {
}