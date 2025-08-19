package trik.testsys.webapp.backoffice.data.dto.impl

import trik.testsys.webapp.backoffice.data.dto.EntityDto
import trik.testsys.webapp.backoffice.data.entity.impl.User

data class UserDto(
    val id: Long?,
    val name: String?,
    val accessTokenValue: String? = null,
    val privileges: MutableSet<User.Privilege> = mutableSetOf(),
) : EntityDto<User>