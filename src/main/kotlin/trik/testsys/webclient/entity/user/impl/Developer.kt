package trik.testsys.webclient.entity.user.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.entity.impl.Contest
import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.entity.impl.TaskFile
import trik.testsys.webclient.entity.user.WebUser
import javax.persistence.*

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Entity
@Table(name = "${TABLE_PREFIX}_DEVELOPER")
class Developer(
    name: String,
    accessToken: AccessToken
) : WebUser(name, accessToken, UserType.DEVELOPER) {

    @OneToMany(mappedBy = "developer", fetch = FetchType.EAGER)
    val tasks: MutableSet<Task> = mutableSetOf()

    /**
     * @author Roman Shishkin
     * @since 2.0.0
     **/
    @OneToMany(mappedBy = "developer", fetch = FetchType.EAGER)
    val contests: MutableSet<Contest> = mutableSetOf()

    /**
     * @author Roman Shishkin
     * @since 2.0.0
     **/
    @OneToMany(mappedBy = "developer", fetch = FetchType.EAGER)
    val taskFiles: MutableSet<TaskFile> = mutableSetOf()

    /**
     * @author Roman Shishkin
     * @since 2.0.0
     **/
    @get:Transient
    val polygons: MutableSet<TaskFile>
        get() = taskFiles.filter { it.type == TaskFile.TaskFileType.POLYGON }.toMutableSet()

    /**
     * @author Roman Shishkin
     * @since 2.0.0
     **/
    @get:Transient
    val exercises: MutableSet<TaskFile>
        get() = taskFiles.filter { it.type == TaskFile.TaskFileType.EXERCISE }.toMutableSet()

    /**
     * @author Roman Shishkin
     * @since 2.0.0
     **/
    @get:Transient
    val solutions: MutableSet<TaskFile>
        get() = taskFiles.filter { it.type == TaskFile.TaskFileType.SOLUTION }.toMutableSet()
}