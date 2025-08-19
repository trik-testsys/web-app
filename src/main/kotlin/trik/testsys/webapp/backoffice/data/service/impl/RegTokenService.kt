package trik.testsys.webapp.backoffice.data.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import trik.testsys.webapp.backoffice.data.entity.impl.RegToken
import trik.testsys.webapp.backoffice.data.repository.RegTokenRepository
import trik.testsys.webapp.backoffice.data.service.TokenService
import trik.testsys.webapp.backoffice.data.service.TokenService.Companion.DEFAULT_SEED
import trik.testsys.webapp.core.data.service.AbstractService
import java.util.Random

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
class RegTokenService :
    AbstractService<RegToken, RegTokenRepository>(),
    TokenService<RegToken> {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun generate(seed: Long?): RegToken {
        val random = Random(seed ?: DEFAULT_SEED)
        val token = RegToken()

        fun nextChunk(): String = Integer.toHexString(random.nextInt(0x10000)).padStart(5, '0')

        do {
            token.value = "${nextChunk()}-${nextChunk()}-${nextChunk()}"
        } while (repository.findByValue(token.value) != null)

        return save(token)
    }

    override fun findByValue(value: String): RegToken? {
        return repository.findByValue(value)
    }
}