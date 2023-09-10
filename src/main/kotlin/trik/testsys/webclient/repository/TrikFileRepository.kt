package trik.testsys.webclient.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import trik.testsys.webclient.entity.Task

import trik.testsys.webclient.entity.TrikFile

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Repository
interface TrikFileRepository : CrudRepository<TrikFile, Long> {

    fun deleteAllByTask(task: Task)
}