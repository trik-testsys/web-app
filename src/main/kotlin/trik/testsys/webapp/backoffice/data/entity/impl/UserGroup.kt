package trik.testsys.webapp.backoffice.data.entity.impl

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import trik.testsys.webapp.core.data.entity.AbstractEntity

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Entity
@Table(name = "user_group")
class UserGroup : AbstractEntity() {

    @PrePersist
    fun prePersist() {
        owner?.let { members.add(it) } ?: error("'owner' field must be initialized")
    }

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(name = "owner_id", nullable = false)
    var owner: User? = null

    @ManyToMany
    @JoinTable(
        name = "${TABLE_PREFIX}user_group_members",
        joinColumns = [JoinColumn(name = "userGroup_id")],
        inverseJoinColumns = [JoinColumn(name = "member_id")]
    )
    var members: MutableSet<User> = mutableSetOf()
}