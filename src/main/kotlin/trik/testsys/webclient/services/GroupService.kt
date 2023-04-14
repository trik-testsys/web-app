package trik.testsys.webclient.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import trik.testsys.webclient.entities.Admin
import trik.testsys.webclient.entities.Group
import trik.testsys.webclient.repositories.GroupRepository
import java.security.MessageDigest
import java.util.*

@Service
class GroupService {

    @Autowired
    private lateinit var groupRepository: GroupRepository

    fun createGroup(admin: Admin, name: String): Group? {
        groupRepository.findGroupByName(name) ?: run {
            val accessToken = generateAccessToken(name)
            val group = Group(admin, name, accessToken)
            return groupRepository.save(group)
        }

        return null
    }

    private fun generateAccessToken(word: String): String {
        val saltedWord = word + Date().time + Random(Date().time).nextInt()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(saltedWord.toByteArray())

        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}