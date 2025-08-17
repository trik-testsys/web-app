package trik.testsys.webclient.entity.impl

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import trik.testsys.sac.data.entity.AbstractEntity
import trik.testsys.sac.data.entity.AbstractEntity.Companion.TABLE_PREFIX

@Entity
@Table(name = "${TABLE_PREFIX}api_key")
class ApiKey(
    @Column(nullable = false, unique = true, updatable = false)
    val value: String
) : AbstractEntity()