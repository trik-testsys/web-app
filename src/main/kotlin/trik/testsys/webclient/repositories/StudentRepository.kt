package trik.testsys.webclient.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

import trik.testsys.webclient.entities.Student
import trik.testsys.webclient.entities.WebUser

@Repository
interface StudentRepository: CrudRepository<Student, String> {

    fun findStudentByWebUser(webUser: WebUser): Student?
}