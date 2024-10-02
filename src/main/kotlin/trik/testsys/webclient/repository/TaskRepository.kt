package trik.testsys.webclient.repository

import org.springframework.stereotype.Repository
import trik.testsys.core.repository.named.NamedEntityRepository
import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.entity.user.impl.Developer


@Repository
interface TaskRepository : NamedEntityRepository<Task> {

    fun findByDeveloper(developer: Developer): List<Task>
}