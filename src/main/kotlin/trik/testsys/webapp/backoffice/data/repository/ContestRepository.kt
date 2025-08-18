//package trik.testsys.webapp.backoffice.data.repository
//
//import org.springframework.stereotype.Repository
//import trik.testsys.core.repository.named.NamedEntityRepository
//import trik.testsys.backoffice.entity.impl.Contest
//import trik.testsys.backoffice.entity.user.impl.Developer
//
///**
// * @author Roman Shishkin
// * @since 2.0.0
// **/
//@Repository
//interface ContestRepository : NamedEntityRepository<Contest> {
//
//    fun findByDeveloper(developer: Developer): List<Contest>
//}