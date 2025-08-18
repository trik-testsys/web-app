package trik.testsys.webapp.backoffice.data.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.StudentGroup
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.repository.StudentGroupRepository
import trik.testsys.webapp.backoffice.data.service.StudentGroupService
import trik.testsys.webapp.core.data.service.AbstractService

@Service
class StudentGroupServiceImpl :
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

    companion object {

        private val logger = LoggerFactory.getLogger(StudentGroupServiceImpl::class.java)
    }
}