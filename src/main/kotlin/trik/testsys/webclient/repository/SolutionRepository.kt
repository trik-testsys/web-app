package trik.testsys.webclient.repository

import org.springframework.stereotype.Repository
import trik.testsys.core.repository.EntityRepository
import trik.testsys.webclient.entity.impl.Solution
import trik.testsys.webclient.entity.impl.Task

@Repository
interface SolutionRepository : EntityRepository<Solution> {

    fun findByTask(task: Task): List<Solution>
}