package trik.testsys.webapp.backoffice.data.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.StudentGroup
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.repository.StudentGroupRepository
import trik.testsys.webapp.backoffice.data.service.StudentGroupService
import trik.testsys.webapp.backoffice.data.service.VerdictService
import trik.testsys.webapp.core.data.service.AbstractService
import trik.testsys.webapp.backoffice.data.service.UserService
import trik.testsys.webapp.backoffice.data.service.UserGroupService

@Service
class StudentGroupServiceImpl(
    private val userService: UserService,
    private val accessTokenService: AccessTokenService,
    private val studentGroupTokenService: StudentGroupTokenService,
    private val userGroupService: UserGroupService,
    private val verdictService: VerdictService,
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
            it.studentGroupToken = studentGroupTokenService.generate()
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
        return generateResultsCsv(listOf(group))
    }

    override fun generateResultsCsv(groups: Collection<StudentGroup>): ByteArray {
        val groupsList = groups.sortedBy { it.id }

        val contests = groupsList.asSequence()
            .flatMap { it.contests.asSequence() }
            .distinctBy { it.id }
            .sortedBy { it.id }
            .toList()

        val contestTaskPairs = contests.flatMap { contest ->
            val orders = contest.getOrders()
            contest.tasks.sortedBy { t -> orders[t.id!!] ?: Long.MAX_VALUE }.map { task -> contest to task }
        }

        val fixedHeader = listOf(
            "student_group_id",
            "student_group_name",
            "admin_id",
            "admin_name",
            "student_id",
            "student_name"
        )
        val dynamicHeader = contestTaskPairs.map { (contest, task) ->
            val cId = contest.id ?: 0
            val cName = contest.name ?: ""
            val tId = task.id ?: 0
            val tName = task.name ?: ""
            "$cId $cName / $tId $tName"
        }
        val header = (fixedHeader + dynamicHeader).joinToString(",") + "\n"

        val membersByGroup = groupsList.associateWith { group ->
            group.members.filter { it.privileges.contains(User.Privilege.STUDENT) }.sortedBy { it.id }
        }

        val allMembers = membersByGroup.values.flatten()

        val contestIds = contests.mapNotNull { it.id }.toSet()
        val taskIds = contestTaskPairs.mapNotNull { it.second.id }.toSet()

        val allRelevantSolutions = allMembers.asSequence()
            .flatMap { it.solutions.asSequence() }
            .filter { s ->
                val cId = s.contest?.id
                val tId = s.task.id
                cId != null && cId in contestIds && tId in taskIds && s.relevantVerdictId != null
            }
            .toList()

        val verdicts = verdictService.findAllBySolutions(allRelevantSolutions)
        val verdictById = verdicts.associateBy { it.id }

        val csv = StringBuilder()
        for ((group, members) in groupsList.map { it to (membersByGroup[it] ?: emptyList()) }) {
            val admin = group.owner
            for (student in members) {
                val fixed = listOf(
                    (group.id ?: 0).toString(),
                    group.name ?: "",
                    (admin?.id ?: 0).toString(),
                    (admin?.name ?: ""),
                    (student.id ?: 0).toString(),
                    student.name ?: ""
                )

                val perTask = contestTaskPairs.map { (contest, task) ->
                    val solution = student.solutions
                        .asSequence()
                        .filter { s ->
                            (s.contest?.id == contest.id) && (s.task.id == task.id) && s.relevantVerdictId != null
                        }
                        .maxByOrNull { s ->
                            verdictById[s.relevantVerdictId]?.value ?: Long.MIN_VALUE
                        }

                    solution?.relevantVerdictId?.let { vid ->
                        verdictById[vid]?.value?.toString()
                    } ?: ""
                }

                csv.append((fixed + perTask).joinToString(","))
                csv.append('\n')
            }
        }

        return (header + csv.toString()).toByteArray()
    }

    companion object {

        private val logger = LoggerFactory.getLogger(StudentGroupServiceImpl::class.java)
    }
}