package trik.testsys.core.entity

import org.springframework.data.jpa.domain.AbstractAuditable
import trik.testsys.core.entity.user.AbstractUser
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class AuditableEntity : AbstractAuditable<AbstractUser, Long>(), TrikEntity