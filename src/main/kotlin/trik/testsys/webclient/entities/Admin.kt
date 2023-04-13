package trik.testsys.webclient.entities

import javax.persistence.*

@Entity
@Table(name = "ADMINS")
data class Admin(
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "web_user_id",
        referencedColumnName = "id",
        nullable = false,
        unique = true
    ) val webUser: WebUser
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    val id: Long? = null

    @Column(
        nullable = false, name = "count_of_groups",
        columnDefinition = "BIGINT DEFAULT 0"
    )
    var countOfGroups: Long = 0L

    @OneToOne(mappedBy = "admin", cascade = [CascadeType.ALL])
    lateinit var group: Group
}