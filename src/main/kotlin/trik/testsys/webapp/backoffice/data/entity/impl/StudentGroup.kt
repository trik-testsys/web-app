package trik.testsys.webapp.backoffice.data.entity.impl

import jakarta.persistence.*
import trik.testsys.webapp.core.data.entity.AbstractEntity
import trik.testsys.webapp.core.data.entity.AbstractEntity.Companion.TABLE_PREFIX

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Entity
@Table(name = "${TABLE_PREFIX}student_group")
class StudentGroup() : AbstractEntity() {

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE], optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    var owner: User? = null

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "${TABLE_PREFIX}student_group_users",
        joinColumns = [JoinColumn(name = "studentGroup_id")],
        inverseJoinColumns = [JoinColumn(name = "members_id")]
    )
    var members: MutableSet<User> = mutableSetOf()
}