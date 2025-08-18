//package trik.testsys.webapp.backoffice.data.repository
//
//import org.springframework.stereotype.Repository
//import trik.testsys.core.repository.named.NamedEntityRepository
//import trik.testsys.backoffice.entity.impl.Task
//import trik.testsys.backoffice.entity.user.impl.Developer
//
//
//@Repository
//interface TaskRepository : NamedEntityRepository<Task> {
//
//    fun findByDeveloper(developer: Developer): List<Task>
//}