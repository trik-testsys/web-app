package trik.testsys.webclient.service.entity.impl

import org.springframework.stereotype.Service
import trik.testsys.core.service.named.AbstractNamedEntityService
import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.entity.user.impl.Developer
import trik.testsys.webclient.repository.TaskRepository

@Service
class TaskService : AbstractNamedEntityService<Task, TaskRepository>() {

    override fun validateName(entity: Task) =
        super.validateName(entity) && !entity.name.contains(entity.developer.accessToken)

    override fun validateAdditionalInfo(entity: Task) =
        super.validateAdditionalInfo(entity) && !entity.additionalInfo.contains(entity.developer.accessToken)

    fun findByDeveloper(developer: Developer) = repository.findByDeveloper(developer)
}