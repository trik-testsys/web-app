package trik.testsys.webapp.backoffice.data.repository.support

import jakarta.persistence.criteria.JoinType
import org.springframework.data.jpa.domain.Specification
import trik.testsys.webapp.backoffice.data.entity.impl.Contest
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.entity.impl.StudentGroup
import trik.testsys.webapp.backoffice.data.entity.impl.User
import java.time.Instant

object SolutionSpecifications {

    /** Solutions whose creator has the STUDENT privilege. */
    fun hasStudentPrivilege(): Specification<Solution> = Specification { root, query, cb ->
        query!!.distinct(true)
        val userJoin = root.join<Solution, User>("createdBy", JoinType.INNER)
        val privJoin = userJoin.join<User, User.Privilege>("privileges", JoinType.INNER)
        cb.equal(privJoin, User.Privilege.STUDENT)
    }

    /** Solutions created by a specific student user id. */
    fun createdBy(studentId: Long): Specification<Solution> = Specification { root, _, cb ->
        cb.equal(root.get<User>("createdBy").get<Long>("id"), studentId)
    }

    /**
     * Solutions where the student belongs to a StudentGroup with [groupId]
     * that also contains the solution's contest.
     */
    fun inGroup(groupId: Long): Specification<Solution> = Specification { root, query, cb ->
        val sub = query!!.subquery(Long::class.java)
        val sg = sub.from(StudentGroup::class.java)
        val member = sg.join<StudentGroup, User>("members", JoinType.INNER)
        val contest = sg.join<StudentGroup, Contest>("contests", JoinType.INNER)
        sub.select(sg.get("id")).where(
            cb.equal(sg.get<Long>("id"), groupId),
            cb.equal(member.get<Long>("id"), root.get<User>("createdBy").get<Long>("id")),
            cb.equal(contest.get<Long>("id"), root.get<Contest>("contest").get<Long>("id"))
        )
        cb.exists(sub)
    }

    /**
     * Solutions where the student's contest group is owned by [adminId].
     */
    fun underAdmin(adminId: Long): Specification<Solution> = Specification { root, query, cb ->
        val sub = query!!.subquery(Long::class.java)
        val sg = sub.from(StudentGroup::class.java)
        val member = sg.join<StudentGroup, User>("members", JoinType.INNER)
        val contest = sg.join<StudentGroup, Contest>("contests", JoinType.INNER)
        sub.select(sg.get("id")).where(
            cb.equal(sg.get<User>("owner").get<Long>("id"), adminId),
            cb.equal(member.get<Long>("id"), root.get<User>("createdBy").get<Long>("id")),
            cb.equal(contest.get<Long>("id"), root.get<Contest>("contest").get<Long>("id"))
        )
        cb.exists(sub)
    }

    /**
     * Solutions where the group admin's viewer is [viewerId].
     */
    fun underViewer(viewerId: Long): Specification<Solution> = Specification { root, query, cb ->
        val sub = query!!.subquery(Long::class.java)
        val sg = sub.from(StudentGroup::class.java)
        val member = sg.join<StudentGroup, User>("members", JoinType.INNER)
        val contest = sg.join<StudentGroup, Contest>("contests", JoinType.INNER)
        sub.select(sg.get("id")).where(
            cb.equal(sg.get<User>("owner").get<User>("viewer").get<Long>("id"), viewerId),
            cb.equal(member.get<Long>("id"), root.get<User>("createdBy").get<Long>("id")),
            cb.equal(contest.get<Long>("id"), root.get<Contest>("contest").get<Long>("id"))
        )
        cb.exists(sub)
    }

    /** Solutions created at or after [from]. */
    fun createdAfter(from: Instant): Specification<Solution> = Specification { root, _, cb ->
        cb.greaterThanOrEqualTo(root.get("createdAt"), from)
    }

    /** Solutions created at or before [to]. */
    fun createdBefore(to: Instant): Specification<Solution> = Specification { root, _, cb ->
        cb.lessThanOrEqualTo(root.get("createdAt"), to)
    }
}
