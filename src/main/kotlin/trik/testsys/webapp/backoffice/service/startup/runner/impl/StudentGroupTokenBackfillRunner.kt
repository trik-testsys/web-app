package trik.testsys.webapp.backoffice.service.startup.runner.impl

import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import trik.testsys.webapp.backoffice.data.entity.impl.StudentGroup
import trik.testsys.webapp.backoffice.data.service.StudentGroupService
import trik.testsys.webapp.backoffice.data.service.impl.StudentGroupTokenService
import trik.testsys.webapp.core.service.startup.AbstractStartupRunner

@Service
@Order(10)
class StudentGroupTokenBackfillRunner(
    private val studentGroupService: StudentGroupService,
    private val studentGroupTokenService: StudentGroupTokenService
) : AbstractStartupRunner() {

    override suspend fun execute() {
        backfillMissingTokens()
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun backfillMissingTokens() {
        val groups: List<StudentGroup> = studentGroupService.findAll()
        val missing = groups.filter { it.studentGroupToken == null }

        if (missing.isEmpty()) {
            logger.info("StudentGroup token backfill: nothing to do.")
            return
        }

        logger.info("StudentGroup token backfill: found ${missing.size} groups without token. Generating...")
        var processed = 0
        missing.forEach { group ->
            try {
                val token = studentGroupTokenService.generate()
                group.studentGroupToken = token
                studentGroupService.save(group)
                processed++
            } catch (e: Exception) {
                logger.error("Failed to backfill token for group id=${group.id}", e)
            }
        }
        logger.info("StudentGroup token backfill: completed. Updated $processed groups.")
    }
}


