package trik.testsys.webclient.entity

import javax.persistence.*

@Entity
@Table(name = "ADMINS")
class Admin(
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "web_user_id",
        referencedColumnName = "id",
        nullable = false,
        unique = true
    ) val webUser: WebUser
) : TrikEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    val id: Long? = null

    @Column(
        nullable = false, name = "count_of_groups",
        columnDefinition = "BIGINT DEFAULT 0"
    )
    var countOfGroups: Long = 0L

    @OneToMany(mappedBy = "admin", cascade = [CascadeType.ALL])
    val groups: MutableSet<Group> = mutableSetOf()

    @ManyToMany(mappedBy = "admins")
    val tasks: MutableSet<Task> = mutableSetOf()

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    @ManyToOne
    @JoinColumn(
        name = "viewer_id",
        referencedColumnName = "id"
    ) lateinit var viewer: Viewer

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    constructor(webUser: WebUser, viewer: Viewer) : this(webUser) {
        this.viewer = viewer
    }
}