package trik.testsys.webapp.backoffice.data.entity.impl

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import trik.testsys.webapp.core.data.entity.AbstractEntity
import trik.testsys.webapp.core.data.entity.AbstractEntity.Companion.TABLE_PREFIX

@Entity
@Table(name = "${TABLE_PREFIX}verdict")
class Verdict() : AbstractEntity() {

    @Column(name = "value", nullable = false)
    var value: Long = -1


    @Column(name = "solution_id", nullable = false)
    var solutionId: Long = -1
}