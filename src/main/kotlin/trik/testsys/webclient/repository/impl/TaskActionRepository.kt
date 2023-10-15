package trik.testsys.webclient.repository.impl

import org.springframework.stereotype.Repository
import trik.testsys.webclient.entity.impl.TaskAction
import trik.testsys.webclient.repository.TrikRepository

@Repository
interface TaskActionRepository : TrikRepository<TaskAction> {
}