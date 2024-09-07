package trik.testsys.webclient.repository

import org.springframework.data.repository.CrudRepository
import trik.testsys.webclient.entity.TrikEntity

/**
 * Repository for all entities that extends [TrikEntity]
 * @author Roman Shishkin
 * @since 1.1.0
 */
interface TrikRepository<T : TrikEntity> : CrudRepository<T, Long> {
}