package trik.testsys.webapp.backoffice.data.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.Random
import trik.testsys.webapp.backoffice.data.entity.impl.AccessToken
import trik.testsys.webapp.backoffice.data.repository.AccessTokenRepository
import trik.testsys.webapp.backoffice.data.service.TokenService
import trik.testsys.webapp.backoffice.data.service.TokenService.Companion.DEFAULT_SEED
import trik.testsys.webapp.core.data.service.AbstractService

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
class AccessTokenService : AbstractService<AccessToken, AccessTokenRepository>(), TokenService<AccessToken> {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun generate(seed: Long?): AccessToken {
        val random = Random(seed ?: DEFAULT_SEED)
        val token = AccessToken()

        fun nextChunk(): String = Integer.toHexString(random.nextInt(0x10000)).padStart(4, '0')

        do {
            token.value = "${nextChunk()}-${nextChunk()}-${nextChunk()}-${nextChunk()}"
        } while (repository.findByValue(token.value) != null)

        return save(token)
    }
}