//package trik.testsys.webapp.backoffice.data.service.impl
//
//import org.springframework.beans.factory.annotation.Qualifier
//import org.springframework.stereotype.Service
//import trik.testsys.core.entity.user.AccessToken
//import trik.testsys.core.service.named.AbstractNamedEntityService
//import trik.testsys.core.service.user.AbstractUserService.Companion.containsAccessToken
//import trik.testsys.backoffice.entity.impl.StudentGroup
//import trik.testsys.backoffice.entity.user.impl.Student
//import trik.testsys.backoffice.repository.GroupRepository
//import trik.testsys.backoffice.service.entity.RegEntityService
//import trik.testsys.backoffice.service.entity.user.impl.StudentService
//import trik.testsys.backoffice.service.token.access.AccessTokenGenerator
//
//@Service
//class GroupService(
//    private val studentService: StudentService,
//    @Qualifier("studentAccessTokenGenerator") private val studentAccessTokenGenerator: AccessTokenGenerator,
//) :
//    RegEntityService<StudentGroup, Student>,
//    AbstractNamedEntityService<StudentGroup, GroupRepository>() {
//
//    override fun findByRegToken(regToken: String) = repository.findByRegToken(regToken)
//
//    override fun register(regToken: AccessToken, name: String): Student? {
//        val group = findByRegToken(regToken) ?: return null
//
//        val accessToken = studentAccessTokenGenerator.generate(name)
//        val student = Student(name, accessToken)
//        student.group = group
//
//        return student.takeIf { studentService.validateName(it) }?.also { studentService.save(it) }
//    }
//
//    override fun validateName(entity: StudentGroup) =
//        super<RegEntityService>.validateName(entity) && super<AbstractNamedEntityService>.validateName(entity) &&
//                !entity.name.containsAccessToken(entity.admin.accessToken)
//
//    override fun validateAdditionalInfo(entity: StudentGroup) =
//        super<RegEntityService>.validateAdditionalInfo(entity) && super<AbstractNamedEntityService>.validateAdditionalInfo(entity) &&
//                !entity.additionalInfo.containsAccessToken(entity.admin.accessToken)
//}