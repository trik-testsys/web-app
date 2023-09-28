package trik.testsys.webclient.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import trik.testsys.webclient.entity.impl.TrikFile
import trik.testsys.webclient.repository.impl.TaskRepository
import trik.testsys.webclient.repository.impl.TrikFileRepository
import trik.testsys.webclient.service.TrikService

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Service
class TrikFileService @Autowired constructor(
    private val trikFileRepository: TrikFileRepository,
    private val taskRepository: TaskRepository,
) : TrikService {

    fun saveAll(files: Set<TrikFile>) {
        trikFileRepository.saveAll(files)
    }

    fun deleteAllByTaskId(taskId: Long): Boolean {
        val task = taskRepository.findTaskById(taskId) ?: return false
        trikFileRepository.deleteAllByTask(task)

        return true
    }
}