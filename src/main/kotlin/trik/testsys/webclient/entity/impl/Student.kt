package trik.testsys.webclient.entity.impl

import javax.persistence.*

@Entity
@Table(name = "STUDENTS")
class Student(
    @ManyToOne
    @JoinColumn(name = "web_user_id", nullable = false, unique = true)
    val webUser: WebUser,

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    val group: Group
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    val id: Long? = null

    @OneToMany(mappedBy = "student", cascade = [CascadeType.ALL])
    val solutions: MutableSet<Solution> = mutableSetOf()

    @OneToMany(mappedBy = "student", cascade = [CascadeType.ALL])
    val taskActions: MutableSet<TaskAction> = mutableSetOf()
}