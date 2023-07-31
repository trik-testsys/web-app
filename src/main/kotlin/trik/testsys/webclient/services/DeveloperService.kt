package trik.testsys.webclient.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import trik.testsys.webclient.repositories.DeveloperRepository

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Service
class DeveloperService @Autowired constructor(
    private val developerRepository: DeveloperRepository
) {
}