package trik.testsys.webclient.entity.impl

import trik.testsys.core.entity.AbstractEntity
import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.webclient.entity.user.WebUser
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "${TABLE_PREFIX}_EMERGENCY_MESSAGE")
class EmergencyMessage(
    @Column(nullable = false, unique = false, updatable = false)
    val userType: WebUser.UserType
) : AbstractEntity()