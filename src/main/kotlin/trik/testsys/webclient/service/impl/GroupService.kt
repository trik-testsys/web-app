package trik.testsys.webclient.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import trik.testsys.webclient.entity.impl.Admin
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.repository.GroupRepository
import java.security.MessageDigest
import java.util.*

@Service
class GroupService @Autowired constructor(
   private val groupRepository: GroupRepository
) {

    fun createGroup(admin: Admin, name: String): Group {
        val accessToken = generateAccessToken(name)
        val group = Group(admin, name, accessToken)
        return groupRepository.save(group)
    }

    fun save(group: Group): Group {
        return groupRepository.save(group)
    }

    fun getGroupByAccessToken(accessToken: String): Group? {
        return groupRepository.findGroupByAccessToken(accessToken)
    }

    fun getGroupById(id: Long): Group? {
        return groupRepository.findGroupById(id)
    }

    private fun generateAccessToken(word: String): String {
        val saltedWord = word + Date().time + Random(Date().time).nextInt()
        val md = MessageDigest.getInstance("SHA-224")
        val digest = md.digest(saltedWord.toByteArray())

        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}