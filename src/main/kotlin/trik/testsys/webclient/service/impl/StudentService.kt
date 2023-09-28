package trik.testsys.webclient.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.entity.impl.Student
import trik.testsys.webclient.entity.impl.WebUser
import trik.testsys.webclient.repository.impl.StudentRepository
import trik.testsys.webclient.repository.impl.WebUserRepository
import trik.testsys.webclient.service.TrikService
import trik.testsys.webclient.util.AccessTokenGenerator

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
) : TrikService {

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

        val prefixRegex = "^$namePrefix$NAME_DELIMITER\\d+$"
        val startNumber = studentRepository.findMaxNumberWithSameNamePrefix(prefixRegex, group.id!!) ?: START_NUMBER_IF_NOT_FOUND

        for (i in 1..count) {
            val number = startNumber + i
            val generatedToken = AccessTokenGenerator.generateAccessToken(namePrefix + number, AccessTokenGenerator.TokenType.WEB_USER)
            val accessToken = accessTokenPrefix + ACCESS_TOKEN_DELIMITER + generatedToken

            val username = "${namePrefix}$NAME_DELIMITER$number"

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
        return studentRepository.findStudentById(id)
    }

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    fun convertToCsv(students: List<Student>): StringBuilder {
        val csv = StringBuilder()
        csv.append("id,username,access_token,group_id,group_name\n")
        students.forEach { student ->
            val webUser = student.webUser
            val group = student.group
            csv.append("${student.id},${webUser.username},${webUser.accessToken},${group.id},${group.name}\n")
        }

        return csv
    }

    companion object {
        private const val HASHING_ALGORITHM_NAME = "MD5"
        private const val START_NUMBER_IF_NOT_FOUND = -1L
        private const val NAME_DELIMITER = "__"
        private const val ACCESS_TOKEN_DELIMITER = "_"
    }
}