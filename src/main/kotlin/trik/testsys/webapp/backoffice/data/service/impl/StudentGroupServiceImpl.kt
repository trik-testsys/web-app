package trik.testsys.webapp.backoffice.data.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.StudentGroup
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.repository.StudentGroupRepository
import trik.testsys.webapp.backoffice.data.service.StudentGroupService
import trik.testsys.webapp.core.data.service.AbstractService
import trik.testsys.webapp.backoffice.data.service.UserService
import trik.testsys.webapp.backoffice.data.service.UserGroupService

@Service
class StudentGroupServiceImpl(
    private val userService: UserService,
    private val accessTokenService: AccessTokenService,
    private val userGroupService: UserGroupService,
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
            val savedGroup = save(studentGroup)

            // If a student is added to a student group, ensure they are membered in all groups of the admin-owner
            if (member.privileges.contains(User.Privilege.STUDENT)) {
                val adminOwner = savedGroup.owner
                adminOwner?.memberedGroups?.forEach { adminGroup ->
                    userGroupService.addMember(adminGroup, member)
                }
            }
            true
        }
    }

    override fun addMembers(studentGroup: StudentGroup, members: User) {
        TODO("Not yet implemented")
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

        val startMs = System.currentTimeMillis()

        // batch-generate tokens to avoid N+1
        val tokens = accessTokenService.generateBatch(count)

        val created = LinkedHashSet<User>(count)
        for ((idx, token) in tokens.withIndex()) {
            val student = User().also {
                it.name = "st-${group.id}-${startMs}-${idx + 1}"
                it.accessToken = token
                it.privileges.add(User.Privilege.STUDENT)
            }
            group.members.add(student)
            created.add(student)
        }

        // Persist students first to avoid transient references in many-to-many joins
        val persistedStudents = userService.saveAll(created)
        val savedGroup = save(group)

        // After persistence, add each student to all user groups where the admin-owner is a member
        val adminOwner = savedGroup.owner
        if (adminOwner != null) {
            val adminGroups = adminOwner.memberedGroups
            if (adminGroups.isNotEmpty()) {
                persistedStudents.forEach { student ->
                    adminGroups.forEach { adminGroup ->
                        userGroupService.addMember(adminGroup, student)
                    }
                }
            }
        }

        return persistedStudents.toSet()
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

    override fun generateResultsCsv(group: StudentGroup): ByteArray {
        val header = "student_id,student_name,task_id,task_name,score,status,last_login_at\n"
        val rows = buildList {
            val members = group.members.sortedBy { it.id }
            // Tasks are attached via group's contests; we cannot traverse tasks directly without a relation here.
            // Emit only student info with placeholders to keep endpoint functional until tasks/solutions are wired.
            members.forEach { student ->
                val id = (student.id ?: 0).toString()
                val name = student.name ?: ""
                val lastLogin = student.lastLoginAt?.toString() ?: ""
                add("$id,$name,,,,$lastLogin")
            }
        }
        return (header + rows.joinToString("\n")).toByteArray()
    }

    companion object {

        private val logger = LoggerFactory.getLogger(StudentGroupServiceImpl::class.java)
    }
}