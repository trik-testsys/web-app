package trik.testsys.webclient.repository

import org.springframework.stereotype.Repository
import trik.testsys.core.repository.named.NamedEntityRepository
import trik.testsys.webclient.entity.impl.TaskFile
import trik.testsys.webclient.entity.user.impl.Developer

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
@Repository
interface TaskFileRepository : NamedEntityRepository<TaskFile> {

    fun findByDeveloper(developer: Developer): List<TaskFile>

    fun findByDeveloperAndType(developer: Developer, type: TaskFile.TaskFileType): List<TaskFile>
}