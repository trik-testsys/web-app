package trik.testsys.webclient.entities

import javax.persistence.*


/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Entity
@Table(name = "DEVELOPERS")
class Developer(
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "web_user_id",
        referencedColumnName = "id",
        nullable = false,
        unique = true
    ) val webUser: WebUser? = null
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    val id: Long? = null

    @OneToMany(mappedBy = "developer", cascade = [CascadeType.ALL])
    val tasks: MutableSet<Task> = mutableSetOf()
}