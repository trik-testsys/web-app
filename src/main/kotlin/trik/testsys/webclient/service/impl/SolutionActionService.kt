package trik.testsys.webclient.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import trik.testsys.webclient.entity.impl.Judge
import trik.testsys.webclient.entity.impl.Solution
import trik.testsys.webclient.entity.impl.SolutionAction
import trik.testsys.webclient.repository.impl.JudgeRepository
import trik.testsys.webclient.repository.impl.SolutionActionRepository
import trik.testsys.webclient.service.TrikService

@Service
class SolutionActionService @Autowired constructor(
    private val solutionActionRepository: SolutionActionRepository,
) : TrikService {

    fun save(solution: Solution, judge: Judge, prevScore: Long, newScore: Long): SolutionAction? {
        val solutionAction = SolutionAction(solution, judge, prevScore, newScore)

        judge.solutionActions.add(solutionAction)
        return solutionActionRepository.save(solutionAction)
    }

    fun getAllJudgeActions(judge: Judge): List<SolutionAction>? {
        return solutionActionRepository.findByJudge(judge)
    }
}
