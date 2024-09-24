package trik.testsys.webclient.view

import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.view.user.UserView
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.entity.user.impl.Student
import trik.testsys.webclient.util.fromTimeZone
import java.time.LocalDateTime
import java.util.TimeZone
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement
data class StudentView(
    override val id: Long?,
    override val name: String,
    override val accessToken: AccessToken,
    val creationDate: LocalDateTime?,
    val lastLoginDate: LocalDateTime,
    val group: Group
) : UserView<Student> {


    override fun toEntity(timeZone: TimeZone) = Student(
        name, accessToken, group
    ).also {
        it.id = id
        it.lastLoginDate = lastLoginDate.fromTimeZone(timeZone)
    }
}