package trik.testsys.core.entity

import org.springframework.data.domain.Persistable
import java.time.LocalDateTime

/**
 * Simple interface for entities. It inherits [getId] (val property id) and [isNew] (val property isNew) from [Persistable],
 * and extends it with [creationDate] field.
 *
 * @author Roman Shishkin
 * @since 2.0.0
 */
interface Entity : Persistable<Long> {

     /**
     * Property which says date and time of entity creation.
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    val creationDate: LocalDateTime
}