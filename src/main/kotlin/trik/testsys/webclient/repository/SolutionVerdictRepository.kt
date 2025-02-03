package trik.testsys.webclient.repository

import org.springframework.stereotype.Repository
import trik.testsys.core.repository.named.NamedEntityRepository
import trik.testsys.webclient.entity.impl.SolutionVerdict
import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.entity.user.impl.Judge
import trik.testsys.webclient.entity.user.impl.Student

@Repository
interface SolutionVerdictRepository : NamedEntityRepository<SolutionVerdict> {

    fun findByJudgeAndStudent(judge: Judge, student: Student): List<SolutionVerdict>

    fun findByStudent(student: Student): List<SolutionVerdict>

    fun findByStudentAndTask(student: Student, task: Task): List<SolutionVerdict>
}