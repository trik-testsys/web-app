package trik.testsys.webclient.repository

import org.springframework.stereotype.Repository
import trik.testsys.core.repository.named.NamedEntityRepository
import trik.testsys.webclient.entity.impl.Contest
import trik.testsys.webclient.entity.user.impl.Developer

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
@Repository
interface ContestRepository : NamedEntityRepository<Contest> {

    fun findByDeveloper(developer: Developer): List<Contest>
}