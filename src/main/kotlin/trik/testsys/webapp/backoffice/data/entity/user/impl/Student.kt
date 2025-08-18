//package trik.testsys.webapp.backoffice.data.entity.user.impl
//
//import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
//import trik.testsys.core.entity.user.AccessToken
//import trik.testsys.backoffice.entity.impl.Contest
//import trik.testsys.backoffice.entity.impl.StudentGroup
//import trik.testsys.backoffice.entity.impl.Solution
//import trik.testsys.backoffice.entity.impl.SolutionVerdict
//import trik.testsys.backoffice.entity.user.WebUser
//import trik.testsys.backoffice.util.toEpochSecond
//import java.time.LocalDateTime
//import java.time.LocalTime
//import javax.persistence.*
//import kotlin.math.max
//import kotlin.math.min
//
//@Entity
//@Table(name = "${TABLE_PREFIX}_STUDENT")
//class Student(
//    name: String,
//    accessToken: AccessToken
//) : WebUser(name, accessToken, UserType.STUDENT) {
//
//    @ManyToOne
//    @JoinColumn(
//        nullable = false, unique = false, updatable = false,
//        name = "group_id", referencedColumnName = "id"
//    )
//    lateinit var group: StudentGroup
//
//    @OneToMany(mappedBy = "student")
//    val solutions: MutableSet<Solution> = mutableSetOf()
//
//    @ElementCollection
//    @MapKeyColumn(name = "contest_id")
//    @Column(name = "start_time")
//    @CollectionTable(
//        name = "CONTESTS_START_TIMES",
//        joinColumns = [JoinColumn(name = "student_id")]
//    )
//    val startTimesByContestId: MutableMap<Long, LocalDateTime> = mutableMapOf()
//
//    @OneToMany(mappedBy = "student")
//    val solutionVerdicts: MutableSet<SolutionVerdict> = mutableSetOf()
//
//    fun startContest(contest: Contest) {
//        startTimesByContestId[contest.id!!] = LocalDateTime.now(DEFAULT_ZONE_ID)
//    }
//
//    fun remainingTimeFor(contest: Contest): LocalTime {
//        val now = LocalDateTime.now(DEFAULT_ZONE_ID)
//        val nowInSeconds = now.toEpochSecond()
//        val contestLastSeconds = contest.lastSeconds
//
//        if (contest.isOpenEnded && contestLastSeconds > 0) return LocalTime.MAX
//
//        val startTime = startTimesByContestId[contest.id!!] ?: return LocalTime.ofSecondOfDay(contestLastSeconds)
//        val startTimeInSeconds = startTime.toEpochSecond()
//
//        val personalRemainingTime = contest.duration.toSecondOfDay() - (nowInSeconds - startTimeInSeconds)
//        val globalRemainingTime = contest.endDate.toEpochSecond() - nowInSeconds
//
//        return LocalTime.ofSecondOfDay(max(0, min(personalRemainingTime, globalRemainingTime)))
//    }
//}