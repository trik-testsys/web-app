package trik.testsys.webclient.entity.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.webclient.entity.AbstractNotedEntity
import trik.testsys.webclient.entity.user.impl.Developer
import javax.persistence.*

@Entity
@Table(name = "${TABLE_PREFIX}_TASK")
class Task(
    name: String
) : AbstractNotedEntity(name) {

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    @ManyToOne
    @JoinColumn(
        nullable = false, unique = false, updatable = false,
        name = "developer_id", referencedColumnName = "id"
    )
    lateinit var developer: Developer
}