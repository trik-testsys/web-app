package trik.testsys.webclient.entities

import javax.persistence.*

@Entity
@Table(name = "SOLUTIONS")
class Solutions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    val id: Long? = null
}