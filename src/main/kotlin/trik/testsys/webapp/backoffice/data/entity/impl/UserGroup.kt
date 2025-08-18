package trik.testsys.webapp.backoffice.data.entity.impl

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import trik.testsys.webapp.core.data.entity.AbstractEntity

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Entity
@Table(name = "user_group")
class UserGroup : AbstractEntity() {

    @ManyToOne(
        fetch = FetchType.LAZY,
        cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE],
        optional = false
    )
    @JoinColumn(name = "owner_id", nullable = false)
    var owner: User? = null

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "${TABLE_PREFIX}user_group_members",
        joinColumns = [JoinColumn(name = "userGroup_id")],
        inverseJoinColumns = [JoinColumn(name = "member_id")]
    )
    var members: MutableSet<User> = mutableSetOf()
}