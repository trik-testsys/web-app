package trik.testsys.webapp.backoffice.data.entity.impl

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import trik.testsys.webapp.core.data.entity.AbstractEntity
import trik.testsys.webapp.core.data.entity.AbstractEntity.Companion.TABLE_PREFIX

@Entity
@Table(name = "${TABLE_PREFIX}verdict")
class Verdict() : AbstractEntity() {

    @Column(name = "value", nullable = false)
    var value: Long = -1

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "solution_id", nullable = false)
    var solution: Solution? = null
}