package trik.testsys.webclient.entity

import javax.persistence.*


/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Entity
@Table(name = "LABELS")
class Label (
    @Column(nullable = false, unique = true, columnDefinition = "VARCHAR(50) DEFAULT ''")
    val name: String
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    val id: Long? = null

    @ManyToMany(mappedBy = "labels")
    val groups: MutableSet<Group> = mutableSetOf()
}