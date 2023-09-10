package trik.testsys.webclient.entity

import javax.persistence.*

@Entity
@Table(name = "WEB_USERS")
class WebUser(
    @Column(
        nullable = false, length = 50,
        columnDefinition = "VARCHAR(50) DEFAULT ''"
    ) val username: String,

    @Column(
        nullable = false, unique = true, length = 50,
        columnDefinition = "VARCHAR(100) DEFAULT ''"
    ) val accessToken: String
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    val id: Long? = null

    @OneToOne(mappedBy = "webUser", cascade = [CascadeType.ALL])
    lateinit var admin: Admin

    @OneToOne(mappedBy = "webUser", cascade = [CascadeType.ALL])
    lateinit var superUser: SuperUser

    @OneToOne(mappedBy = "webUser", cascade = [CascadeType.ALL])
    lateinit var developer: Developer

    @OneToMany(mappedBy = "webUser", cascade = [CascadeType.ALL])
    val students: MutableSet<Student> = mutableSetOf()

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    enum class Status {
        NOT_FOUND,
        ADMIN,
        SUPER_USER,
        WEB_USER,
        DEVELOPER
    }
}