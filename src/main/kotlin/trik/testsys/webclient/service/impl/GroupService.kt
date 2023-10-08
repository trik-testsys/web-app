package trik.testsys.webclient.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import trik.testsys.webclient.entity.impl.Admin
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.repository.impl.GroupRepository
import trik.testsys.webclient.service.TrikService
import trik.testsys.webclient.util.AccessTokenGenerator

@Service
class GroupService @Autowired constructor(
   private val groupRepository: GroupRepository
) : TrikService {

    fun createGroup(admin: Admin, name: String, additionalInfo: String?): Group {
        val accessToken = AccessTokenGenerator.generateAccessToken(name, AccessTokenGenerator.TokenType.GROUP)
        val group = Group(admin, name, accessToken, additionalInfo)
        return groupRepository.save(group)
    }

    fun createGroup(admin: Admin, name: String): Group {
        val accessToken = AccessTokenGenerator.generateAccessToken(name, AccessTokenGenerator.TokenType.GROUP)
        val group = Group(admin, name, accessToken)
        return groupRepository.save(group)
    }

    fun save(group: Group): Group {
        return groupRepository.save(group)
    }

    fun saveAll(groups: Collection<Group>): List<Group> {
        return groupRepository.saveAll(groups).toList()
    }

    fun getGroupByAccessToken(accessToken: String): Group? {
        return groupRepository.findGroupByAccessToken(accessToken)
    }

    fun getGroupById(id: Long): Group? {
        return groupRepository.findGroupById(id)
    }

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    fun getAllByIds(ids: List<Long>): List<Group> {
        return groupRepository.findAllById(ids).toList()
    }

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    fun delete(group: Group) {
        groupRepository.delete(group)
    }
}