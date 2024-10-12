package trik.testsys.webclient.service.entity.impl

import org.springframework.stereotype.Service
import trik.testsys.core.service.AbstractService
import trik.testsys.webclient.entity.impl.Solution
import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.repository.SolutionRepository

@Service
class SolutionService : AbstractService<Solution, SolutionRepository>() {

    fun findTaskTests(task: Task): List<Solution> {
        val allTaskSolutions = repository.findByTask(task)
        val taskTests = allTaskSolutions.filter { it.isTest }

        return taskTests
    }
}