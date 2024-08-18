package trik.testsys.webclient.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

import trik.testsys.webclient.entity.impl.Task

@Repository
interface TaskRepository: CrudRepository<Task, Long> {

    fun findTaskById(id: Long): Task?
}