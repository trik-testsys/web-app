package trik.testsys.webclient.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import trik.testsys.webclient.entities.WebUser

@Repository
interface WebUserRepository : CrudRepository<WebUser, String> {

    fun findWebUserById(id: Long): WebUser?

    fun findWebUserByAccessToken(accessToken: String): WebUser?
}