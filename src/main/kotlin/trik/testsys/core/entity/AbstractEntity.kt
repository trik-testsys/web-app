package trik.testsys.core.entity

import java.time.LocalDateTime
import java.time.ZoneId
import javax.persistence.*

/**
 * Simple abstract entity class. Describes basic entity behavior.
 *
 * @author Roman Shishkin
 * @since 2.0.0
 */
@MappedSuperclass
abstract class AbstractEntity : Entity {

    /**
     * Entity ID field.
     * May be null if an entity doesn't persist in a database.
     *
     * The default value is null.
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
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