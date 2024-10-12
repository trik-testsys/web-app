package trik.testsys.webclient.repository

import org.springframework.stereotype.Repository
import trik.testsys.core.repository.EntityRepository
import trik.testsys.webclient.entity.impl.Solution
import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.entity.user.impl.Student

@Repository
interface SolutionRepository : EntityRepository<Solution> {

    fun findByTask(task: Task): List<Solution>

    fun findByStudent(student: Student): List<Solution>

    fun findByStudentAndTask(student: Student, task: Task): List<Solution>
}