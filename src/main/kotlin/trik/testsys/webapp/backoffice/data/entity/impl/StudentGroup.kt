package trik.testsys.webapp.backoffice.data.entity.impl

import jakarta.persistence.*
import trik.testsys.webapp.core.data.entity.AbstractEntity
import trik.testsys.webapp.core.data.entity.AbstractEntity.Companion.TABLE_PREFIX

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
@Entity
@Table(name = "${TABLE_PREFIX}student_group")
class StudentGroup() : AbstractEntity() {

    @Column(name = "name")
    var name: String? = null

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    var owner: User? = null

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "${TABLE_PREFIX}student_group_users",
        joinColumns = [JoinColumn(name = "studentGroup_id")],
        inverseJoinColumns = [JoinColumn(name = "members_id")]
    )
    var members: MutableSet<User> = mutableSetOf()

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "${TABLE_PREFIX}student_group_contests",
        joinColumns = [JoinColumn(name = "studentGroup_id")],
        inverseJoinColumns = [JoinColumn(name = "contest_id")]
    )
    var contests: MutableSet<Contest> = mutableSetOf()

    @OneToOne(fetch = FetchType.EAGER, optional = true, orphanRemoval = true)
    @JoinColumn(name = "student_group_token_id", nullable = true, unique = true)
    var studentGroupToken: StudentGroupToken? = null
}