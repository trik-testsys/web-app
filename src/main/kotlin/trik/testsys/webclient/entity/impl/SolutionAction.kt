package trik.testsys.webclient.entity.impl//package trik.testsys.webclient.entity.impl
//
//import trik.testsys.webclient.entity.TrikEntity
//import trik.testsys.webclient.entity.user.impl.Judge
//import java.time.LocalDateTime
//import java.time.ZoneOffset.UTC
//import javax.persistence.*
//
//@Entity
//@Table(name = "SOLUTION_ACTIONS")
//class SolutionAction(
//    @ManyToOne(cascade = [CascadeType.ALL])
//    @JoinColumn(name = "solution_id", referencedColumnName = "id", nullable = false)
//    val solution: Solution,
//
//    @ManyToOne(cascade = [CascadeType.ALL])
//    @JoinColumn(name = "judge_id", referencedColumnName = "id", nullable = false)
//    val judge: Judge,
//
//    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
//    var prevScore: Long,
//    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
//    var newScore: Long
//) : TrikEntity {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(nullable = false, unique = true)
//    val id: Long? = null
//
//    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
//    val dateTime: LocalDateTime = LocalDateTime.now(UTC)
//        get() = field.plusHours(3)
//}