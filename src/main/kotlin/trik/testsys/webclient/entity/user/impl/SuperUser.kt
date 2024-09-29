package trik.testsys.webclient.entity.user.impl

import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.entity.user.WebUser
import trik.testsys.webclient.enums.UserType
import javax.persistence.*

@Entity
@Table(name = "SUPER_USERS")
class SuperUser(
    name: String,
    accessToken: AccessToken
): WebUser(name, accessToken, UserType.SUPER_USER)