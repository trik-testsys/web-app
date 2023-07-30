package trik.testsys.webclient.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import trik.testsys.webclient.entities.Group
import trik.testsys.webclient.entities.Student
import trik.testsys.webclient.entities.WebUser
import trik.testsys.webclient.repositories.StudentRepository
import trik.testsys.webclient.repositories.WebUserRepository
import java.security.MessageDigest
import java.util.*
import kotlin.random.Random

/**
 * @author Roman Shishkin
 * @since 1.0.0
 */
@Service
class StudentService @Autowired constructor(
    private val studentRepository: StudentRepository,
    private val webUserRepository: WebUserRepository
) {

    fun getByWebUser(webUser: WebUser): Student? {
        return studentRepository.findByWebUser(webUser)
    }

    fun save(webUser: WebUser, group: Group): Student {
        return studentRepository.save(Student(webUser, group))
    }

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     * @param count count of students to generate
     * @param accessTokenPrefix prefix for access token
     * @param namePrefix prefix for username
     * @param group group to attach students to
     */
    fun generateStudents(count: Long, accessTokenPrefix: String, namePrefix: String, group: Group): List<Student> {
        val students = mutableListOf<Student>()
        val webUsers = mutableListOf<WebUser>()

        for (number in 0 until count) {
            val accessToken = generateAccessToken(accessTokenPrefix, namePrefix)
            val username = namePrefix + number

            val webUser = WebUser(username, accessToken)
            webUsers.add(webUser)

            val student = Student(webUser, group)
            students.add(student)
        }

        webUserRepository.saveAll(webUsers)
        studentRepository.saveAll(students)

        return students
    }

    fun getById(id: Long): Student? {
        return studentRepository.findById(id)
    }

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     * @param accessTokenPrefix prefix for access token
     * @param usernamePrefix prefix for username
     */
    private fun generateAccessToken(accessTokenPrefix: String, usernamePrefix: String): String {
        val saltedWord = usernamePrefix + Random(Date().time).nextInt()
        val md = MessageDigest.getInstance(HASHING_ALGORITHM_NAME)

        val hash = md.digest(saltedWord.toByteArray())
        val foldedHash = hash.fold("") { str, it -> str + "%02x".format(it) }

        return "${accessTokenPrefix}_$foldedHash"
    }

    companion object {
        private const val HASHING_ALGORITHM_NAME = "SHA-1"
    }
}