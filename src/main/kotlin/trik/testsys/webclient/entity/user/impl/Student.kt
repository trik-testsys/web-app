package trik.testsys.webclient.entity.user.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.entity.impl.Contest
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.entity.impl.Solution
import trik.testsys.webclient.entity.user.WebUser
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import javax.persistence.*
import kotlin.math.min

@Entity
@Table(name = "${TABLE_PREFIX}_STUDENT")
class Student(
    name: String,
    accessToken: AccessToken
) : WebUser(name, accessToken, UserType.STUDENT) {

    @ManyToOne
    @JoinColumn(
        nullable = false, unique = false, updatable = false,
        name = "group_id", referencedColumnName = "id"
    )
    lateinit var group: Group

    @OneToMany(mappedBy = "student", cascade = [CascadeType.ALL])
    val solutions: MutableSet<Solution> = mutableSetOf()

    @ElementCollection
    @MapKeyColumn(name = "contest_id")
    @Column(name = "start_time")
    @CollectionTable(
        name = "CONTESTS_START_TIMES",
        joinColumns = [JoinColumn(name = "student_id")]
    )
    val startTimesByContestId: MutableMap<Long, LocalDateTime> = mutableMapOf()

    fun startContest(contest: Contest) {
        startTimesByContestId[contest.id!!] = LocalDateTime.now(DEFAULT_ZONE_ID)
    }

    fun lastTime(contest: Contest): LocalTime {
        val lastTimeForContest = contest.endDate.toEpochSecond(ZoneOffset.UTC) - LocalDateTime.now(DEFAULT_ZONE_ID).toEpochSecond(ZoneOffset.UTC)
        val contestLastTime = contest.duration.toSecondOfDay().toLong()

        val startTime = startTimesByContestId[contest.id!!] ?: return LocalTime.ofSecondOfDay(min(contestLastTime, lastTimeForContest))
        val usedSeconds =  LocalDateTime.now(DEFAULT_ZONE_ID).toEpochSecond(ZoneOffset.UTC) - startTime.toEpochSecond(ZoneOffset.UTC)

        val lastSeconds = contest.duration.toSecondOfDay() - usedSeconds

        val realLastTime = min(lastSeconds, lastTimeForContest)

        return LocalTime.ofSecondOfDay(realLastTime)
    }
//
//    @OneToMany(mappedBy = "student", cascade = [CascadeType.ALL])
//    val taskActions: MutableSet<TaskAction> = mutableSetOf()
}