package trik.testsys.webapp.backoffice.data.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.entity.impl.Verdict
import trik.testsys.webapp.backoffice.data.repository.SolutionRepository
import trik.testsys.webapp.backoffice.data.repository.VerdictRepository
import trik.testsys.webapp.backoffice.data.service.VerdictService
import trik.testsys.webapp.backoffice.data.service.SolutionService
import trik.testsys.webapp.core.data.service.AbstractService

@Service
class VerdictServiceImpl(
    private val solutionRepository: SolutionRepository,
) : AbstractService<Verdict, VerdictRepository>(), VerdictService {

    @Transactional(propagation = Propagation.REQUIRED)
    override fun createNewForSolution(solution: Solution, score: Long): Verdict {
        val newVerdict = Verdict().also {
            it.solutionId = solution.id ?: error("Solution id must be initialized")
            it.value = score
        }

        val persisted = repository.save(newVerdict)

        solution.relevantVerdictId = newVerdict.id
        solutionRepository.save(solution)

        return persisted
    }

    override fun findAllBySolutionIds(solutionIds: Collection<Long>): Set<Verdict> {
        val solutions = solutionRepository.findAllById(solutionIds)
        return findAllBySolutions(solutions)
    }

    override fun findAllBySolutions(solutions: Collection<Solution>): Set<Verdict> {
        val verdictIds = solutions.mapNotNull { it.relevantVerdictId }
        val verdicts = repository.findAllById(verdictIds)

        return verdicts.toSet()
    }
}