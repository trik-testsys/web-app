//package trik.testsys.webapp.backoffice.data.entity.impl
//
//import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
//import trik.testsys.core.utils.enums.converter.AbstractEnumConverter
//import trik.testsys.backoffice.entity.AbstractNotedEntity
//import trik.testsys.backoffice.entity.User
//import trik.testsys.backoffice.util.*
//import java.time.LocalDateTime
//import java.time.LocalTime
//import jakarta.persistence.*
//import kotlin.math.min
//
//
//@Entity
//@Table(name = "${TABLE_PREFIX}_CONTEST")
//class Contest(
//    name: String,
//
//    @Column(nullable = false, unique = false, updatable = true)
//    var startDate: LocalDateTime,
//
//    @Column(nullable = false, unique = false, updatable = true)
//    var endDate: LocalDateTime,
//
//    @Column(nullable = false, unique = false, updatable = true)
//    var duration: LocalTime,
//) : AbstractNotedEntity(name) {
//
//    @ManyToOne
//    @JoinColumn(
//        nullable = false, unique = false, updatable = true,
//        name = "developer_id", referencedColumnName = "id"
//    )
//    lateinit var developer: User
//
//    @Column(nullable = false, unique = false, updatable = true)
//    var visibility: Visibility = Visibility.PRIVATE
//
//    fun switchVisibility() {
//        visibility = visibility.opposite()
//    }
//
//    fun isPublic() = visibility == Visibility.PUBLIC
//
//    @ManyToMany
//    @JoinTable(
//        name = "CONTESTS_BY_GROUPS",
//        joinColumns = [JoinColumn(name = "contest_id")],
//        inverseJoinColumns = [JoinColumn(name = "group_id")]
//    )
//    val groups: MutableSet<StudentGroup> = mutableSetOf()
//
//    fun isGoingOn(): Boolean {
//        val now = LocalDateTime.now(DEFAULT_ZONE_ID)
//        return startDate.isBeforeOrEqual(now) && endDate.isAfterOrEqual(now)
//    }
//
//    fun isOutdatedFor(student: User): Boolean {
//        val studentRemainingTime = student.remainingTimeFor(this)
//        return studentRemainingTime.toSecondOfDay() == 0
//    }
//
//    @get:Transient
//    val durationInSeconds: Long
//        get() {
//            return endDate.toEpochSecond() - startDate.toEpochSecond() + 1
//        }
//
//    @get:Transient
//    val lastSeconds: Long
//        get() {
//            if (!isGoingOn()) {
//                return 0
//            }
//
//            val now = LocalDateTime.now(DEFAULT_ZONE_ID)
//            val lastSeconds = endDate.toEpochSecond() - now.toEpochSecond() + 1
//
//            return if (isOpenEnded) lastSeconds else min(duration.toSecondOfDay().toLong(), lastSeconds)
//        }
//
//    @ElementCollection
//    @MapKeyColumn(name = "student_id")
//    @Column(name = "start_time")
//    @CollectionTable(
//        name = "CONTESTS_START_TIMES",
//        joinColumns = [JoinColumn(name = "contest_id")]
//    )
//    val startTimesByStudentId: MutableMap<Long, LocalDateTime> = mutableMapOf()
//
//    @ManyToMany(mappedBy = "contests")
//    val tasks: MutableSet<Task> = mutableSetOf()
//
//    /**
//     * @author Roman Shishkin
//     * @since %CURRENT_VERSION%
//     */
//    var isOpenEnded: Boolean = false
//
//    enum class Visibility(override val dbkey: String) : Enum {
//
//        PUBLIC("PLC"),
//        PRIVATE("PRV");
//
//        fun opposite() = when (this) {
//            PUBLIC -> PRIVATE
//            PRIVATE -> PUBLIC
//        }
//
//        @Converter(autoApply = true)
//        class VisibilityConverter : AbstractEnumConverter<Visibility>()
//    }
//}