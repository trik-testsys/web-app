package trik.testsys.webapp.backoffice.data.entity.impl

import jakarta.persistence.*
import trik.testsys.webapp.backoffice.data.entity.Sharable
import trik.testsys.webapp.core.data.entity.AbstractEntity
import trik.testsys.webapp.core.data.entity.AbstractEntity.Companion.TABLE_PREFIX
import java.time.Instant

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Entity
@Table(name = "${TABLE_PREFIX}contest")
class Contest() :
    AbstractEntity(),
    Sharable {

    @Column(name = "name", nullable = false)
    var name: String? = null

    @Column(name = "starts_at", nullable = false)
    var startsAt: Instant? = null

    @Column(name = "ends_at")
    var endsAt: Instant? = null

    @Column(name = "duration")
    var duration: Long? = null

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "developer_id", nullable = false)
    var developer: User? = null

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "ts_contest_userGroups",
        joinColumns = [JoinColumn(name = "contest_id")],
        inverseJoinColumns = [JoinColumn(name = "userGroups_id")]
    )
    override var userGroups: MutableSet<UserGroup> = mutableSetOf()
}