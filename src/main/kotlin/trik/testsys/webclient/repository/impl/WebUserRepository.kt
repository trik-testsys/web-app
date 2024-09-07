package trik.testsys.webclient.repository.impl

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import trik.testsys.webclient.entity.impl.WebUser

/**
 * @author Roman Shishkin
 * @since 1.0.0
 */
@Repository
interface WebUserRepository : CrudRepository<WebUser, Long> {

    fun findWebUserById(id: Long): WebUser?

    fun findWebUserByAccessToken(accessToken: String): WebUser?
}