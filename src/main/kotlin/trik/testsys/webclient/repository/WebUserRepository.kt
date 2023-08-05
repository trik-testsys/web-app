package trik.testsys.webclient.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import trik.testsys.webclient.entity.WebUser

/**
 * @author Roman Shishkin
 * @since 1.0.0
 */
@Repository
interface WebUserRepository : CrudRepository<WebUser, Long> {

    fun findWebUserById(id: Long): WebUser?

    fun findWebUserByAccessToken(accessToken: String): WebUser?
}