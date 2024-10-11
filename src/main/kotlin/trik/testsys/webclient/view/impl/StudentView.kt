package trik.testsys.webclient.view.impl

import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.view.user.UserView
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.entity.user.impl.Student
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.util.fromTimeZone
import java.time.LocalDateTime

data class StudentView(
    override val id: Long?,
    override val name: String,
    override val accessToken: AccessToken,
    override val creationDate: LocalDateTime?,
    override val lastLoginDate: LocalDateTime?,
    override val additionalInfo: String,
    val group: Group
) : UserView<Student> {


    override fun toEntity(timeZoneId: String?) = Student(
        name, accessToken
    ).also {
        it.id = id
        it.lastLoginDate = lastLoginDate?.fromTimeZone(timeZoneId)
        it.additionalInfo = additionalInfo
        it.group = group
    }

    companion object {
        fun Student.toView(timeZoneId: String?) = StudentView(
            id,
            name,
            accessToken,
            creationDate?.atTimeZone(timeZoneId),
            lastLoginDate?.atTimeZone(timeZoneId),
            additionalInfo,
            group
        )
    }
}