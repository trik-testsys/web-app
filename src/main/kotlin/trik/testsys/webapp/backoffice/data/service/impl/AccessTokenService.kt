package trik.testsys.webapp.backoffice.data.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import trik.testsys.webapp.backoffice.data.entity.Token
import java.security.SecureRandom
import trik.testsys.webapp.backoffice.data.entity.impl.AccessToken
import trik.testsys.webapp.backoffice.data.repository.AccessTokenRepository
import trik.testsys.webapp.backoffice.data.service.TokenService
import trik.testsys.webapp.core.data.service.AbstractService

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
class AccessTokenService : AbstractService<AccessToken, AccessTokenRepository>(), TokenService<AccessToken> {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun generate(): AccessToken {
        val random = SecureRandom()
        val token = AccessToken()

        fun nextChunk(): String = Integer.toHexString(random.nextInt(0x10000)).padStart(4, '0')

        do {
            token.value = "${nextChunk()}-${nextChunk()}-${nextChunk()}-${nextChunk()}"
        } while (findByValue(token.value!!) != null)

        return save(token)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun generateBatch(count: Int): List<AccessToken> {
        require(count >= 1) { "count must be >= 1" }

        val random = SecureRandom()

        fun nextChunk(): String = Integer.toHexString(random.nextInt(0x10000)).padStart(4, '0')

        val tokens = ArrayList<AccessToken>(count)
        val values = LinkedHashSet<String>(count)

        // generate optimistic set of values (allow temporary dups, will re-fill later)
        while (values.size < count) {
            values.add("${nextChunk()}-${nextChunk()}-${nextChunk()}-${nextChunk()}")
        }

        // ensure uniqueness against DB and within batch
        val existing = repository.findByValueIn(values)
            .mapNotNull { it.value }
            .toHashSet()

        if (existing.isNotEmpty()) {
            // remove existing values and regenerate until no collisions
            values.removeAll(existing)
            while (values.size < count) {
                val candidate = "${nextChunk()}-${nextChunk()}-${nextChunk()}-${nextChunk()}"
                if (candidate !in existing && candidate !in values) {
                    values.add(candidate)
                }
            }
        }

        for (v in values) {
            val t = AccessToken()
            t.value = v
            tokens.add(t)
        }

        return saveAll(tokens)
    }

    override fun findByValue(value: String): AccessToken? {
        return repository.findByValueAndType(value, Token.Type.ACCESS)
    }
}