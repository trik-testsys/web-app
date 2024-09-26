package trik.testsys.webclient.service.entity.impl

import org.springframework.stereotype.Service
import trik.testsys.core.service.AbstractService

import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.repository.GroupRepository

@Service
class GroupService : AbstractService<Group, GroupRepository>() {

    fun findByAccessToken(accessToken: String): Group? {
        return repository.findByAccessToken(accessToken)
    }
}