package trik.testsys.webclient.service.entity.impl

import org.springframework.stereotype.Service
import trik.testsys.core.service.named.AbstractNamedEntityService
import trik.testsys.core.service.user.AbstractUserService.Companion.containsAccessToken
import trik.testsys.webclient.entity.impl.Contest
import trik.testsys.webclient.entity.user.impl.Developer
import trik.testsys.webclient.repository.ContestRepository

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
@Service
class ContestService : AbstractNamedEntityService<Contest, ContestRepository>() {

    fun findByDeveloper(developer: Developer) = repository.findByDeveloper(developer)

    override fun validateName(entity: Contest) =
        super.validateName(entity) && !entity.name.containsAccessToken(entity.developer.accessToken)

    override fun validateAdditionalInfo(entity: Contest) =
        super.validateAdditionalInfo(entity) && !entity.additionalInfo.containsAccessToken(entity.developer.accessToken)

    fun findAllPublic() = repository.findAll().filter { it.visibility == Contest.Visibility.PUBLIC }
}