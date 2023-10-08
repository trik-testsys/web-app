package trik.testsys.webclient.repository

import trik.testsys.webclient.entity.TrikEntity

/**
 * @since 1.1.0
 * @author Roman Shishkin
 */
interface UserRepository<T: TrikEntity>: TrikRepository<T> {
}