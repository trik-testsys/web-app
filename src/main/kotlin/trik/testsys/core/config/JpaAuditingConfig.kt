package trik.testsys.core.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * Configuration enabling Spring Data JPA auditing.
 *
 * With this enabled, fields annotated with `@CreatedDate` (and others like `@LastModifiedDate`
 * if used) are populated automatically by the persistence layer.
 *
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Configuration
@EnableJpaAuditing(modifyOnCreate = true)
@EnableTransactionManagement
@EntityScan(basePackages = ["trik.testsys.**.entity"])
@EnableJpaRepositories(basePackages = ["trik.testsys.**.repository"])
class JpaAuditingConfig
