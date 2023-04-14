package trik.testsys.webclient.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import trik.testsys.webclient.entities.Student
import trik.testsys.webclient.entities.WebUser
import trik.testsys.webclient.repositories.StudentRepository

@Service
class StudentService {

    @Autowired
    private lateinit var studentRepository: StudentRepository

    fun getStudentByWebUser(webUser: WebUser): Student? {
        return studentRepository.findStudentByWebUser(webUser)
    }
}