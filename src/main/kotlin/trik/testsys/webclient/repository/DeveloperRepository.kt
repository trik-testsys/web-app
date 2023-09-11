package trik.testsys.webclient.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import trik.testsys.webclient.entity.Developer
import trik.testsys.webclient.entity.WebUser

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Repository
interface DeveloperRepository: CrudRepository<Developer, Long> {

    fun findByWebUser(webUser: WebUser): Developer?
}