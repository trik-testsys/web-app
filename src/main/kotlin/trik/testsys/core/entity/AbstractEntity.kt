package trik.testsys.core.entity

import java.time.LocalDateTime
import java.time.ZoneId
import javax.persistence.*

@MappedSuperclass
abstract class AbstractEntity : Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    var id: Long? = null
        private set

    override fun getId() = id

    override fun isNew() = id == null

    @Column(nullable = false, columnDefinition = "DATETIME")
    override val creationDate: LocalDateTime = LocalDateTime.now(DEFAULT_ZONE)

    override fun toString() = when (isNew) {
        true -> "New entity of type ${javaClass.name} and with hashcode: ${hashCode().toString(16)}"
        false -> "Entity of type ${javaClass.name} and with ID: $id"
    }

    companion object {
        private const val DEFAULT_ZONE_ID = "UTC"
        private val DEFAULT_ZONE = ZoneId.of(DEFAULT_ZONE_ID)
    }
}