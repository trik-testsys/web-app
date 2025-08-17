package trik.testsys.webclient.repository

import org.springframework.stereotype.Repository
import trik.testsys.sac.data.repository.EntityRepository
import trik.testsys.webclient.entity.impl.ApiKey

@Repository
interface ApiKeyRepository : EntityRepository<ApiKey> {

    fun findByValue(value: String): ApiKey?
}