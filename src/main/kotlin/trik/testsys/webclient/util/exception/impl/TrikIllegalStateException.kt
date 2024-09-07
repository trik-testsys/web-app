package trik.testsys.webclient.util.exception.impl

import trik.testsys.webclient.util.exception.TrikException

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
class TrikIllegalStateException(message: String) : IllegalStateException(message), TrikException