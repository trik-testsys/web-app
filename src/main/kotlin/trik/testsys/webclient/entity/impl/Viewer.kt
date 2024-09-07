package trik.testsys.webclient.entity.impl

import javax.persistence.*

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Entity
@Table(name = "VIEWERS")
class Viewer(
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "web_user_id",
        referencedColumnName = "id",
        nullable = false,
        unique = true
    ) val webUser: WebUser,
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    val id: Long? = null

    @OneToMany(mappedBy = "viewer", cascade = [CascadeType.ALL])
    val admins: MutableSet<Admin> = mutableSetOf()

    @Column(nullable = false, name = "admin_reg_token", columnDefinition = "VARCHAR(100) DEFAULT ''")
    var adminRegToken: String = ""
}