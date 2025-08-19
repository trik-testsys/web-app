package trik.testsys.webapp.backoffice.service.mapper.impl

import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.dto.impl.UserDto
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.service.mapper.ObjectMapper

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
class UserObjectMapper : ObjectMapper<User, UserDto> {

    override fun toDto(entity: User?): UserDto {
        return UserDto(
            entity?.id,
            entity?.name,
            entity?.accessToken?.value,
            entity?.privileges ?: mutableSetOf()
        )
    }

    override fun toEntity(dto: UserDto): User {
        return User().also {
            it.id = dto.id
            it.name = dto.name
            it.privileges.addAll(dto.privileges)
        }
    }
}