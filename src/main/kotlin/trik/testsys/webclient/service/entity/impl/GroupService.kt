package trik.testsys.webclient.service.entity.impl

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.service.AbstractService
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.entity.user.impl.Student
import trik.testsys.webclient.repository.GroupRepository
import trik.testsys.webclient.repository.user.StudentRepository
import trik.testsys.webclient.service.entity.RegEntityService
import trik.testsys.webclient.service.token.access.AccessTokenGenerator

@Service
class GroupService(
    private val studentRepository: StudentRepository,
    @Qualifier("studentAccessTokenGenerator") private val studentAccessTokenGenerator: AccessTokenGenerator,
) :
    RegEntityService<Group, Student>,
    AbstractService<Group, GroupRepository>() {

    override fun findByRegToken(regToken: String) = repository.findByRegToken(regToken)

    override fun register(regToken: AccessToken, name: String, validationBlock: (Student) -> Boolean): Student? {
        val group = findByRegToken(regToken) ?: return null

        val accessToken = studentAccessTokenGenerator.generate(name)
        val student = Student(name, accessToken)
        student.group = group

        return student.takeIf(validationBlock)?.also { studentRepository.save(it) }
    }
}