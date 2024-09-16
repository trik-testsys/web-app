package trik.testsys.webclient.entity.impl

import trik.testsys.core.entity.user.AbstractUser
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.utils.marker.TrikEntity
import javax.persistence.*

@Entity
@Table(name = "TS_WEB_USER")
class WebUser(
    override var name: String,
    override var accessToken: AccessToken
) : AbstractUser(name, accessToken), TrikEntity {

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
    @OneToOne(mappedBy = "webUser", cascade = [CascadeType.ALL])
    lateinit var viewer: Viewer

    @OneToOne(mappedBy = "webUser", cascade = [CascadeType.ALL])
    lateinit var judge: Judge

//    /**
//     * @author Roman Shishkin
//     * @since 1.1.0
//     */
//    enum class Status {
//        NOT_FOUND,
//        ADMIN,
//        SUPER_USER,
//        WEB_USER,
//        DEVELOPER
//    }
}