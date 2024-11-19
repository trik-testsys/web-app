package trik.testsys.webclient.entity.impl

import trik.testsys.core.entity.AbstractEntity
import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "${TABLE_PREFIX}_API_KEY")
class ApiKey(
    @Column(nullable = false, unique = true, updatable = false)
    val value: String
) : AbstractEntity()