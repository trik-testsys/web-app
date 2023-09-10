package trik.testsys.webclient.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import trik.testsys.webclient.entity.Task

import trik.testsys.webclient.entity.TrikFile
import trik.testsys.webclient.repository.TaskRepository
import trik.testsys.webclient.repository.TrikFileRepository

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Service
class TrikFileService @Autowired constructor(
    private val trikFileRepository: TrikFileRepository,
    private val taskRepository: TaskRepository,
) {

    fun saveAll(files: Set<TrikFile>) {
        trikFileRepository.saveAll(files)
    }

    fun deleteAllByTask(taskId: Long): Boolean {
        val task = taskRepository.findTaskById(taskId) ?: return false
        trikFileRepository.deleteAllByTask(task)

        return true
    }
}