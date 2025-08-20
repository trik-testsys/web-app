package trik.testsys.webapp.backoffice.data.entity.impl

import jakarta.persistence.Column
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

    @Column(name = "name")
    var name: String? = null

    /**
     * Flag for default group for each user. Better be only one default group in system.
     */
    @Column(name = "is_default", nullable = false)
    var defaultGroup: Boolean = false

    @PrePersist
    fun prePersist() {
        owner?.let { members.add(it) } ?: error("'owner' field must be initialized")
    }

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    var owner: User? = null

    @ManyToMany
    @JoinTable(
        name = "${TABLE_PREFIX}user_group_members",
        joinColumns = [JoinColumn(name = "userGroup_id")],
        inverseJoinColumns = [JoinColumn(name = "member_id")]
    )
    var members: MutableSet<User> = mutableSetOf()

    @ManyToMany(mappedBy = "userGroups")
    var contests: MutableSet<Contest> = mutableSetOf()
}