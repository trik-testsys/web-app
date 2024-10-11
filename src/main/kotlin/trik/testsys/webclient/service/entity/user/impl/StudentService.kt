package trik.testsys.webclient.service.entity.user.impl

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.entity.user.impl.Student
import trik.testsys.webclient.repository.user.StudentRepository
import trik.testsys.webclient.service.entity.user.WebUserService
import trik.testsys.webclient.service.token.access.AccessTokenGenerator

/**
 * @author Roman Shishkin
 * @since 1.0.0
 */
@Service
class StudentService(
    @Qualifier("studentAccessTokenGenerator") private val accessTokenGenerator: AccessTokenGenerator
): WebUserService<Student, StudentRepository>() {

    override fun validateName(entity: Student) =
        !entity.name.contains(entity.group.regToken) && super.validateName(entity)

    override fun validateAdditionalInfo(entity: Student) =
        !entity.additionalInfo.contains(entity.group.regToken) && super.validateAdditionalInfo(entity)

    fun generate(count: Long, group: Group): List<Student> {
        val students = mutableListOf<Student>()

        for (i in 1..count) {
            val number = i
            val accessToken = accessTokenGenerator.generate(number.toString() + group.regToken)
            val name = "st_${group.name}_$number"

            val student = Student(name, accessToken)
            student.group = group

            students.add(student)
        }

        repository.saveAll(students)

        return students
    }

//    /**
//     * @author Roman Shishkin
//     * @since 1.1.0
//     * @param count count of students to generate
//     * @param accessTokenPrefix prefix for access token
//     * @param namePrefix prefix for username
//     * @param group group to attach students to
//     */
//    fun generateStudents(count: Long, accessTokenPrefix: String, namePrefix: String, group: Group): List<Student> {
//        val students = mutableListOf<Student>()
//        val webUsers = mutableListOf<WebUser>()
//
////        val noramalizedAccessTokenPrefix = accessTokenPrefix.replace(" ", "-")
////        val noramalizedNamePrefix = namePrefix.replace(" ", "-")
//
////        val prefixRegex = "^$noramalizedNamePrefix\\d+$"
////        val startNumber = studentRepository.findMaxNumberWithSameNamePrefix(prefixRegex, group.id!!) ?: START_NUMBER_IF_NOT_FOUND
//
//        for (i in 1..count) {
//            val number = i
//            val generatedToken = AccessTokenGenerator.generateAccessToken(
//                number.toString(),
//                AccessTokenGenerator.TokenType.STUDENT
//            )
//            val accessToken = generatedToken
//
//            val username = "st_${group.name}_$number"
//
//            val webUser = WebUser(username, accessToken)
//            webUsers.add(webUser)
//
//            val student = Student(webUser, group)
//            students.add(student)
//        }
//
//        webUserService.saveAll(webUsers)
//        repository.saveAll(students)
//
//        return students
//    }

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    fun convertToCsv(students: List<Student>): StringBuilder {
        val csv = StringBuilder()
        csv.append("id,username,access_token,group_id,group_name\n")
        students.forEach { student ->
            val group = student.group
            csv.append("${student.id},${student.name},${student.accessToken},${group.id},${group.name}\n")
        }

        return csv
    }
}