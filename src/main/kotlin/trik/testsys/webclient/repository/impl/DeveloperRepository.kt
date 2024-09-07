package trik.testsys.webclient.repository.impl

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import trik.testsys.webclient.entity.impl.Developer
import trik.testsys.webclient.entity.impl.WebUser

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Repository
interface DeveloperRepository: CrudRepository<Developer, Long> {

    fun findByWebUser(webUser: WebUser): Developer?
}