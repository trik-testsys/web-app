package trik.testsys.webapp.core.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.NoRepositoryBean
import trik.testsys.webapp.core.data.entity.AbstractEntity

/**
 * Base Spring Data repository for all entities extending [AbstractEntity].
 *
 * Extends [JpaRepository] for CRUD operations and [JpaSpecificationExecutor]
 * for flexible criteria queries. Marked as [NoRepositoryBean] so Spring
 * does not try to instantiate it directly.
 *
 * Consumers should extend this interface for their concrete entities, e.g.:
 * `interface ProjectRepository : BaseRepository<ProjectEntity>`
 *
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@NoRepositoryBean
interface EntityRepository<T : AbstractEntity> : JpaRepository<T, Long>, JpaSpecificationExecutor<T>

