package trik.testsys.webapp.backoffice.data.entity.impl

import jakarta.persistence.*
import trik.testsys.webapp.core.data.entity.AbstractEntity
import trik.testsys.webapp.core.data.entity.AbstractEntity.Companion.TABLE_PREFIX
import java.time.Instant

@Entity
@Table(name = "${TABLE_PREFIX}contest_run")
class ContestRun : AbstractEntity() {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    lateinit var user: User

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "contest_id", nullable = false)
    lateinit var contest: Contest

    @Column(name = "started_at", nullable = false)
    lateinit var startedAt: Instant
}


