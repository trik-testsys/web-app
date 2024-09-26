package trik.testsys.webclient.repository

import org.springframework.stereotype.Repository

import trik.testsys.webclient.entity.user.impl.Admin
import trik.testsys.webclient.entity.impl.Group

@Repository
interface GroupRepository: trik.testsys.core.repository.Repository<Group> {

    fun findGroupsByAdmin(admin: Admin): List<Group>?

    fun findByAccessToken(accessToken: String): Group?
}