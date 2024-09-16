package trik.testsys.core.entity

import java.time.LocalDateTime
import java.time.ZoneId
import javax.persistence.*

/**
 * Simple abstract entity class. Describes basic entity behavior.
 *
 * @see Entity
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
    private var id: Long? = null

    override fun getId() = id

    override fun isNew() = id == null

    @Column(nullable = false, unique = false, columnDefinition = "DATETIME")
    override val creationDate: LocalDateTime = LocalDateTime.now(DEFAULT_ZONE)

    @Column(
        nullable = false, unique = false, length = ADDITIONAL_INFO_MAX_LENGTH,
        columnDefinition = "VARCHAR(${ADDITIONAL_INFO_MAX_LENGTH}) DEFAULT ''"
    )
    override var additionalInfo: String = ADDITIONAL_INFO_DEFAULT

    override fun toString() = when (isNew) {
        true -> "New entity of type ${javaClass.name} and with hashcode: ${hashCode().toString(16)}"
        false -> "Entity of type ${javaClass.name} and with ID: $id"
    }

    companion object {

        private const val DEFAULT_ZONE_ID = "UTC"
        private val DEFAULT_ZONE = ZoneId.of(DEFAULT_ZONE_ID)

        private const val ADDITIONAL_INFO_MAX_LENGTH = 1000
        private const val ADDITIONAL_INFO_DEFAULT = ""
    }
}