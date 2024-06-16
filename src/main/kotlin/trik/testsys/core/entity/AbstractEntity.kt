package trik.testsys.core.entity

import org.springframework.data.jpa.domain.AbstractPersistable
import javax.persistence.*

@MappedSuperclass
abstract class AbstractEntity : AbstractPersistable<Long>(), TrikEntity