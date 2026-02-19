package trik.testsys.webapp.backoffice.data.entity.impl

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import trik.testsys.webapp.backoffice.data.entity.Sharable
import trik.testsys.webapp.core.data.entity.AbstractEntity
import trik.testsys.webapp.core.data.entity.AbstractEntity.Companion.TABLE_PREFIX
 
import java.time.Instant

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Entity
@Table(name = "${TABLE_PREFIX}contest")
class Contest() :
    AbstractEntity(),
    Sharable {

    @Column(name = "name", nullable = false)
    var name: String? = null

    @Column(name = "starts_at", nullable = false)
    var startsAt: Instant? = null

    @Column(name = "ends_at")
    var endsAt: Instant? = null

    @Column(name = "duration", nullable = true)
    var duration: Long? = null

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "developer_id", nullable = false)
    var developer: User? = null

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "ts_contest_userGroups",
        joinColumns = [JoinColumn(name = "contest_id")],
        inverseJoinColumns = [JoinColumn(name = "userGroups_id")]
    )
    override var userGroups: MutableSet<UserGroup> = mutableSetOf()

    @OneToMany(mappedBy = "contest", orphanRemoval = true)
    var solutions: MutableSet<Solution> = mutableSetOf()

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "ts_contest_tasks",
        joinColumns = [JoinColumn(name = "contest_id")],
        inverseJoinColumns = [JoinColumn(name = "tasks_id")]
    )
    var tasks: MutableSet<Task> = mutableSetOf()

    @Column(name = "data", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    val data: Data = Data()

    fun getOrders(): Map<Long, Long> {
        // If no explicit order is set, fall back to id order with 1..N labels
        if (data.orderByTaskId.isEmpty()) {
            var idx = 0L
            return tasks
                .sortedBy { it.id }
                .associate { task -> task.id!! to (++idx) }
        }

        // There is some saved order: normalize to 1..N without mutating entity
        val attachedIds = tasks.mapNotNull { it.id }.toSet()
        val bySaved = data.orderByTaskId
            .filterKeys { it in attachedIds }
            .toList()
            .sortedBy { it.second }

        val result = LinkedHashMap<Long, Long>(tasks.size)
        var pos = 1L
        for ((taskId, _) in bySaved) {
            result[taskId] = pos
            pos += 1
        }

        val remaining = tasks
            .sortedBy { it.id }
            .mapNotNull { it.id }
            .filterNot { it in result.keys }
        for (taskId in remaining) {
            result[taskId] = pos
            pos += 1
        }

        return result
    }

    data class Data(
        val orderByTaskId: MutableMap<Long, Long> = mutableMapOf()
    )
}