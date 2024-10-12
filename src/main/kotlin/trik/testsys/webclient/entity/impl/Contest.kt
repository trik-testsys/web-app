package trik.testsys.webclient.entity.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.utils.enums.Enum
import trik.testsys.core.utils.enums.converter.AbstractEnumConverter
import trik.testsys.webclient.entity.AbstractNotedEntity
import trik.testsys.webclient.entity.user.impl.Developer
import java.time.LocalDateTime
import java.time.LocalTime
import javax.persistence.*


@Entity
@Table(name = "${TABLE_PREFIX}_CONTEST")
class Contest(
    name: String,

    @Column(nullable = false, unique = false, updatable = true)
    var startDate: LocalDateTime,

    @Column(nullable = false, unique = false, updatable = true)
    var endDate: LocalDateTime,

    @Column(nullable = false, unique = false, updatable = true)
    var duration: LocalTime,
) : AbstractNotedEntity(name) {

    @ManyToOne
    @JoinColumn(
        nullable = false, unique = false, updatable = true,
        name = "developer_id", referencedColumnName = "id"
    )
    lateinit var developer: Developer

    @Column(nullable = false, unique = false, updatable = true)
    var visibility: Visibility = Visibility.PRIVATE

    fun switchVisibility() {
        visibility = visibility.opposite()
    }

    fun isPublic() = visibility == Visibility.PUBLIC

    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinTable(
        name = "CONTESTS_BY_GROUPS",
        joinColumns = [JoinColumn(name = "contest_id")],
        inverseJoinColumns = [JoinColumn(name = "group_id")]
    )
    val groups: MutableSet<Group> = mutableSetOf()

    fun isGoingOn() = startDate <= LocalDateTime.now(DEFAULT_ZONE_ID) && endDate >= LocalDateTime.now(DEFAULT_ZONE_ID)

    @ElementCollection
    @MapKeyColumn(name = "student_id")
    @Column(name = "start_time")
    @CollectionTable(
        name = "CONTESTS_START_TIMES",
        joinColumns = [JoinColumn(name = "contest_id")]
    )
    val startTimesByStudentId: MutableMap<Long, LocalDateTime> = mutableMapOf()

    @ManyToMany(cascade = [CascadeType.ALL], mappedBy = "contests", fetch = FetchType.EAGER)
    val tasks: MutableSet<Task> = mutableSetOf()

    enum class Visibility(override val dbkey: String) : Enum {

        PUBLIC("PLC"),
        PRIVATE("PRV");

        fun opposite() = when (this) {
            PUBLIC -> PRIVATE
            PRIVATE -> PUBLIC
        }

        @Converter(autoApply = true)
        class VisibilityConverter : AbstractEnumConverter<Visibility>()
    }
}