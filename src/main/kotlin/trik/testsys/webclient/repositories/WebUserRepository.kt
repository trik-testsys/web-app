package trik.testsys.webclient.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import trik.testsys.webclient.entities.WebUser

/**
 * @author Roman Shishkin
 * @since 1.0.0
 */
@Repository
interface WebUserRepository : CrudRepository<WebUser, Long> {

    fun findWebUserById(id: Long): WebUser?

    fun findWebUserByAccessToken(accessToken: String): WebUser?
}