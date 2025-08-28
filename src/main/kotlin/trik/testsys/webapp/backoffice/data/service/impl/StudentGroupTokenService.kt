package trik.testsys.webapp.backoffice.data.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import trik.testsys.webapp.backoffice.data.entity.impl.StudentGroupToken
import trik.testsys.webapp.backoffice.data.repository.StudentGroupTokenRepository
import trik.testsys.webapp.backoffice.data.service.TokenService
import trik.testsys.webapp.core.data.service.AbstractService
import java.security.SecureRandom

@Service
class StudentGroupTokenService :
    AbstractService<StudentGroupToken, StudentGroupTokenRepository>(),
    TokenService<StudentGroupToken> {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun generate(): StudentGroupToken {
        val random = SecureRandom()
        val token = StudentGroupToken()

        fun nextChunk(): String = Integer.toHexString(random.nextInt(0x10000)).padStart(6, '0')

        do {
            token.value = "${nextChunk()}-${nextChunk()}"
        } while (repository.findByValue(token.value) != null)

        return save(token)
    }

    override fun findByValue(value: String): StudentGroupToken? {
        return repository.findByValue(value)
    }
}


