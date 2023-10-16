package trik.testsys.webclient.repository.impl

import org.springframework.stereotype.Repository
import trik.testsys.webclient.entity.impl.Solution
import trik.testsys.webclient.entity.impl.Student
import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.entity.impl.TaskAction
import trik.testsys.webclient.repository.TrikRepository

@Repository
interface TaskActionRepository : TrikRepository<TaskAction> {

    fun findAllByStudentAndTask(student: Student, task: Task): List<TaskAction>?

    fun findAllByStudentAndSolution(student: Student, solution: Solution): List<TaskAction>?
}