package trik.testsys.webclient.service.entity.impl

import org.springframework.stereotype.Service
import trik.testsys.core.service.named.AbstractNamedEntityService
import trik.testsys.webclient.entity.impl.SolutionVerdict
import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.entity.user.impl.Judge
import trik.testsys.webclient.entity.user.impl.Student
import trik.testsys.webclient.repository.SolutionVerdictRepository

@Service
class SolutionVerdictService : AbstractNamedEntityService<SolutionVerdict, SolutionVerdictRepository>() {

    fun findByJudgeAndStudent(judge: Judge, student: Student) = repository.findByJudgeAndStudent(judge, student)

    fun findByStudent(student: Student) = repository.findByStudent(student)

    fun findByStudentAndTask(student: Student, task: Task) = repository.findByStudentAndTask(student, task)
}