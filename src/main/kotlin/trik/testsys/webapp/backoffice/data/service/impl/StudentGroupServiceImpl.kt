package trik.testsys.webapp.backoffice.data.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.StudentGroup
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.repository.StudentGroupRepository
import trik.testsys.webapp.backoffice.data.service.StudentGroupService
import trik.testsys.webapp.core.data.service.AbstractService
import trik.testsys.webapp.backoffice.data.service.UserService

@Service
class StudentGroupServiceImpl(
    private val userService: UserService,
    private val accessTokenService: AccessTokenService,
) :
    AbstractService<StudentGroup, StudentGroupRepository>(),
    StudentGroupService {

    override fun addMember(studentGroup: StudentGroup, member: User) = when (member) {
        studentGroup.owner -> {
            logger.warn("Could not add user(id=${member.id}) to owned studentGroup(id=${studentGroup.id}).")
            false
        }
        in studentGroup.members -> {
            logger.warn("Could not add already membered user(id=${member.id}) to studentGroup(id=${studentGroup.id}).")
            false
        }
        else -> {
            logger.debug("Adding user(id=${member.id}) to studentGroup(id=${studentGroup.id})")

            studentGroup.members.add(member)
            save(studentGroup)
            true
        }
    }

    override fun removeMember(studentGroup: StudentGroup, member: User) = when (member) {
        studentGroup.owner -> {
            logger.warn("Could not remove user(id=${member.id}) from owned studentGroup(id=${studentGroup.id}) members.")
            false
        }
        !in studentGroup.members -> {
            logger.warn("Could not remove user(id=${member.id}) from not membered studentGroup(id=${studentGroup.id}).")
            false
        }
        else -> {
            logger.debug("Removing member(id=${member.id}) from studentGroup(id=${studentGroup.id}).")

            studentGroup.members.remove(member)
            save(studentGroup)
            true
        }
    }

    override fun findByOwner(owner: User): Set<StudentGroup> {
        return repository.findByOwner(owner)
    }

    override fun create(owner: User, name: String, info: String?): StudentGroup? {
        val newGroup = StudentGroup().also {
            it.owner = owner
            it.name = name.trim()
            it.info = info?.trim()
        }

        return save(newGroup)
    }

    override fun generateStudents(owner: User, group: StudentGroup, count: Int): Set<User> {
        require(count >= 1) { "count must be >= 1" }
        require(count <= 200) { "count must be <= 200" }

        val created = mutableSetOf<User>()
        repeat(count) { idx ->
            val token = accessTokenService.generate(owner.id)
            val student = User().also {
                it.name = "st-${group.id}-${System.currentTimeMillis()}-${idx + 1}"
                it.accessToken = token
                it.privileges.add(User.Privilege.STUDENT)
            }

            val persisted = userService.save(student)
            group.members.add(persisted)
            created.add(persisted)
        }

        save(group)
        return created
    }

    override fun generateMembersCsv(group: StudentGroup): ByteArray {
        val header = "user_id,user_name,access_token\n"
        val body = group.members.joinToString("\n") {
            val id = (it.id ?: 0).toString()
            val name = it.name ?: ""
            val token = it.accessToken?.value ?: ""
            "$id,$name,$token"
        }
        return (header + body).toByteArray()
    }

    companion object {

        private val logger = LoggerFactory.getLogger(StudentGroupServiceImpl::class.java)
    }
}