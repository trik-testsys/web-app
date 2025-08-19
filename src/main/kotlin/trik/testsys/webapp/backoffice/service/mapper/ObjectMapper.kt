package trik.testsys.webapp.backoffice.service.mapper

import trik.testsys.webapp.backoffice.data.dto.EntityDto
import trik.testsys.webapp.core.data.entity.AbstractEntity

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
interface ObjectMapper<E, DTO> where E : AbstractEntity, DTO : EntityDto<E> {

    fun toDto(entity: E?): DTO

    fun toEntity(dto: DTO): E
}