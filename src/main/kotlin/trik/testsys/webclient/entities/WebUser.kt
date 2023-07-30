package trik.testsys.webclient.entities

import trik.testsys.webclient.models.WebUserModel
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

    @OneToMany(mappedBy = "webUser", cascade = [CascadeType.ALL])
    val students: MutableSet<Student> = mutableSetOf()

    fun toModel(): WebUserModel {
        return WebUserModel(
            id = id!!,
            username = username,
            accessToken = accessToken
        )
    }
}