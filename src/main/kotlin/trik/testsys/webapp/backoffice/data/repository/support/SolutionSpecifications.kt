package trik.testsys.webapp.backoffice.data.repository.support

import jakarta.persistence.criteria.JoinType
import org.springframework.data.jpa.domain.Specification
import trik.testsys.webapp.backoffice.data.entity.impl.Contest
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.entity.impl.StudentGroup
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.core.data.entity.AbstractEntity
import java.time.Instant

object SolutionSpecifications {

    /** Solutions whose creator has the STUDENT privilege. */
    fun hasStudentPrivilege(): Specification<Solution> = Specification { root, query, cb ->
        query?.distinct(true)
        val userJoin = root.join<Solution, User>("createdBy", JoinType.INNER)
        val privJoin = userJoin.join<User, User.Privilege>(User.PRIVILEGES, JoinType.INNER)
        cb.equal(privJoin, User.Privilege.STUDENT)
    }

    /** Solutions created by a specific student user id. */
    fun createdBy(studentId: Long): Specification<Solution> = Specification { root, _, cb ->
        cb.equal(root.get<User>("createdBy").get<Long>(AbstractEntity.ID), studentId)
    }

    /**
     * Solutions where the student belongs to a StudentGroup with [groupId]
     * that also contains the solution's contest.
     *
     * Note: solutions with a null contest (test/non-contest submissions) are excluded from
     * results because the subquery predicate on `Solution.contest` evaluates to NULL.
     */
    fun inGroup(groupId: Long): Specification<Solution> = Specification { root, query, cb ->
        query ?: return@Specification cb.conjunction()
        val sub = query.subquery(Long::class.java)
        val sg = sub.from(StudentGroup::class.java)
        val member = sg.join<StudentGroup, User>("members", JoinType.INNER)
        val contest = sg.join<StudentGroup, Contest>("contests", JoinType.INNER)
        sub.select(sg.get<Long>(AbstractEntity.ID)).where(
            cb.equal(sg.get<Long>(AbstractEntity.ID), groupId),
            cb.equal(member.get<Long>(AbstractEntity.ID), root.get<User>("createdBy").get<Long>(AbstractEntity.ID)),
            cb.equal(contest.get<Long>(AbstractEntity.ID), root.get<Contest>("contest").get<Long>(AbstractEntity.ID))
        )
        cb.exists(sub)
    }

    /**
     * Solutions where the student's contest group is owned by [adminId].
     *
     * Note: solutions with a null contest (test/non-contest submissions) are excluded from
     * results because the subquery predicate on `Solution.contest` evaluates to NULL.
     */
    fun underAdmin(adminId: Long): Specification<Solution> = Specification { root, query, cb ->
        query ?: return@Specification cb.conjunction()
        val sub = query.subquery(Long::class.java)
        val sg = sub.from(StudentGroup::class.java)
        val member = sg.join<StudentGroup, User>("members", JoinType.INNER)
        val contest = sg.join<StudentGroup, Contest>("contests", JoinType.INNER)
        sub.select(sg.get<Long>(AbstractEntity.ID)).where(
            cb.equal(sg.get<User>("owner").get<Long>(AbstractEntity.ID), adminId),
            cb.equal(member.get<Long>(AbstractEntity.ID), root.get<User>("createdBy").get<Long>(AbstractEntity.ID)),
            cb.equal(contest.get<Long>(AbstractEntity.ID), root.get<Contest>("contest").get<Long>(AbstractEntity.ID))
        )
        cb.exists(sub)
    }

    /**
     * Solutions where the group admin's viewer is [viewerId].
     *
     * Note: solutions with a null contest (test/non-contest submissions) are excluded from
     * results because the subquery predicate on `Solution.contest` evaluates to NULL.
     */
    fun underViewer(viewerId: Long): Specification<Solution> = Specification { root, query, cb ->
        query ?: return@Specification cb.conjunction()
        val sub = query.subquery(Long::class.java)
        val sg = sub.from(StudentGroup::class.java)
        val member = sg.join<StudentGroup, User>("members", JoinType.INNER)
        val contest = sg.join<StudentGroup, Contest>("contests", JoinType.INNER)
        sub.select(sg.get<Long>(AbstractEntity.ID)).where(
            cb.equal(sg.get<User>("owner").get<User>("viewer").get<Long>(AbstractEntity.ID), viewerId),
            cb.equal(member.get<Long>(AbstractEntity.ID), root.get<User>("createdBy").get<Long>(AbstractEntity.ID)),
            cb.equal(contest.get<Long>(AbstractEntity.ID), root.get<Contest>("contest").get<Long>(AbstractEntity.ID))
        )
        cb.exists(sub)
    }

    /** Solutions created at or after [from]. */
    fun createdAfter(from: Instant): Specification<Solution> = Specification { root, _, cb ->
        cb.greaterThanOrEqualTo(root.get(AbstractEntity.CREATED_AT), from)
    }

    /** Solutions created at or before [to]. */
    fun createdBefore(to: Instant): Specification<Solution> = Specification { root, _, cb ->
        cb.lessThanOrEqualTo(root.get(AbstractEntity.CREATED_AT), to)
    }
}
