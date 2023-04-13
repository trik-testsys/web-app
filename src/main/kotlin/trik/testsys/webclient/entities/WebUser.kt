package trik.testsys.webclient.entities

import javax.persistence.*

@Entity
@Table(name = "WEB_USERS")
data class WebUser(
    @Column(
        nullable = false, length = 50,
        columnDefinition = "VARCHAR(50) DEFAULT ''"
    ) val username: String,

    @Column(
        nullable = false, unique = true, length = 50,
        columnDefinition = "VARCHAR(50) DEFAULT ''"
    ) val accessToken: String
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    val id: Long? = null

    @OneToOne(mappedBy = "webUser", cascade = [CascadeType.ALL])
    lateinit var admin: Admin

    @OneToMany(mappedBy = "webUser", cascade = [CascadeType.ALL])
    lateinit var students: Set<Student>
}