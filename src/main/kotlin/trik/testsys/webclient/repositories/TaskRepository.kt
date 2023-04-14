package trik.testsys.webclient.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

import trik.testsys.webclient.entities.Task

@Repository
interface TaskRepository: CrudRepository<Task, String> {

    fun findTaskById(id: Long): Task?
}