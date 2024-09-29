package trik.testsys.webclient.view

import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.view.user.UserView
import trik.testsys.webclient.entity.user.impl.Student
import trik.testsys.webclient.util.fromTimeZone
import java.time.LocalDateTime
import java.util.TimeZone

data class StudentView(
    override val id: Long?,
    override val name: String,
    override val accessToken: AccessToken,
    override val creationDate: LocalDateTime?,
    override val lastLoginDate: LocalDateTime?,
    val group: GroupView,
    val additionalInfo: String
) : UserView<Student> {


    override fun toEntity(timeZone: TimeZone) = Student(
        name, accessToken
    ).also {
        it.id = id
        it.lastLoginDate = lastLoginDate?.fromTimeZone(timeZone)
        it.additionalInfo = additionalInfo
    }
}