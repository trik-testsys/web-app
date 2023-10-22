package trik.testsys.webclient.entity.impl

import trik.testsys.webclient.entity.TrikEntity
import javax.persistence.*


@Entity
@Table(name = "JUDGES")
class Judge(
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
}