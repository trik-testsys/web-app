package trik.testsys.webclient.view.impl

import trik.testsys.webclient.entity.impl.TaskFile
import trik.testsys.webclient.entity.user.impl.Developer

data class TaskFileCreationView(
    val name: String,
    val additionalInfo: String,
    val note: String,
    val type: TaskFile.TaskFileType
) {

    fun toEntity(developer: Developer) = TaskFile(
        name, type
    ).also {
        it.additionalInfo = additionalInfo
        it.note = note
        it.developer = developer
    }

    companion object {

        fun emptyPolygon() = empty(TaskFile.TaskFileType.POLYGON)

        fun emptyExercise() = empty(TaskFile.TaskFileType.EXERCISE)

        fun emptySolution() = empty(TaskFile.TaskFileType.SOLUTION)

        fun empty(type: TaskFile.TaskFileType) = TaskFileCreationView("", "", "", type)
    }
}