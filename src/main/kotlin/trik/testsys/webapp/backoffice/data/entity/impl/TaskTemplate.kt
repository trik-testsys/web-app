package trik.testsys.webapp.backoffice.data.entity.impl

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import trik.testsys.webapp.backoffice.data.entity.Sharable
import trik.testsys.webapp.core.data.entity.AbstractEntity
import trik.testsys.webapp.core.data.entity.AbstractEntity.Companion.TABLE_PREFIX


@Entity
@Table(name = "${TABLE_PREFIX}task_template")
class TaskTemplate() :
    AbstractEntity(),
    Sharable {

    @Column(name = "name")
    var name: String? = null

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "developer_id", nullable = false)
    var developer: User? = null

    @OneToMany(mappedBy = "createdFrom", orphanRemoval = true)
    var createdTasks: MutableSet<Task> = mutableSetOf()

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "ts_task_template_userGroups",
        joinColumns = [JoinColumn(name = "task_template_id")],
        inverseJoinColumns = [JoinColumn(name = "userGroups_id")]
    )
    override var userGroups: MutableSet<UserGroup> = mutableSetOf()
}