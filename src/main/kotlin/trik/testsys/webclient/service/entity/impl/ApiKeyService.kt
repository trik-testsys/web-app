package trik.testsys.webclient.service.entity.impl

import org.springframework.stereotype.Service
import trik.testsys.core.service.AbstractService
import trik.testsys.webclient.entity.impl.ApiKey
import trik.testsys.webclient.repository.ApiKeyRepository

@Service
class ApiKeyService : AbstractService<ApiKey, ApiKeyRepository>() {

    fun findByValue(value: String): ApiKey? {
        return repository.findByValue(value)
    }

    fun validate(value: String): Boolean {
        return findByValue(value) != null
    }
}