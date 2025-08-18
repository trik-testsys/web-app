//package trik.testsys.webapp.backoffice.data.service.impl
//
//import org.springframework.stereotype.Service
//import trik.testsys.core.service.named.AbstractNamedEntityService
//import trik.testsys.backoffice.entity.impl.Solution
//import trik.testsys.backoffice.entity.impl.Task
//import trik.testsys.backoffice.entity.user.impl.Developer
//import trik.testsys.backoffice.repository.TaskRepository
//import java.time.LocalDateTime
//
//@Service
//class TaskService(
//    private val solutionService: SolutionService
//) : AbstractNamedEntityService<Task, TaskRepository>() {
//
//    override fun validateName(entity: Task) =
//        super.validateName(entity) && !entity.name.contains(entity.developer.accessToken)
//
//    override fun validateAdditionalInfo(entity: Task) =
//        super.validateAdditionalInfo(entity) && !entity.additionalInfo.contains(entity.developer.accessToken)
//
//    fun findByDeveloper(developer: Developer) = repository.findByDeveloper(developer)
//
//    fun getLastTest(task: Task): Solution? {
//        val solutions = solutionService.findTaskTests(task)
//        val lastTest = solutions.maxByOrNull { it.creationDate ?: LocalDateTime.MIN }
//
//        return lastTest
//    }
//}