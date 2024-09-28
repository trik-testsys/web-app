package trik.testsys.core.entity

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.TimeZone
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
    @Column(nullable = false, unique = true, updatable = false)
    private var id: Long? = null

    @Column(nullable = true, unique = false, updatable = false)
    override var creationDate: LocalDateTime? = null

    override fun setId(id: Long?) {
        this.id = id
    }

    override fun getId() = id

    override fun isNew() = id == null

    @Column(
        nullable = false, unique = false, updatable = true,
        length = ADDITIONAL_INFO_MAX_LENGTH
    )
    override var additionalInfo: String = ADDITIONAL_INFO_DEFAULT

    override fun toString() = when (isNew) {
        true -> "New entity of type ${javaClass.name} and with hashcode: ${hashCode().toString(16)}"
        false -> "Entity of type ${javaClass.name} and with ID: $id"
    }

    @PrePersist
    fun onCreate() {
        creationDate = LocalDateTime.now(DEFAULT_ZONE_ID)
    }

    companion object {

        const val DEFAULT_ZONE_CODE = "UTC"
        val DEFAULT_ZONE_ID: ZoneId = ZoneId.of(DEFAULT_ZONE_CODE)
        val DEFAULT_TIME_ZONE: TimeZone = TimeZone.getTimeZone(DEFAULT_ZONE_ID)

        private const val ADDITIONAL_INFO_MAX_LENGTH = 1000
        private const val ADDITIONAL_INFO_DEFAULT = ""
    }
}