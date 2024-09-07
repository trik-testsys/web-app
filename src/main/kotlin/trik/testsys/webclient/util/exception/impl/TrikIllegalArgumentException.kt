package trik.testsys.webclient.util.exception.impl

import trik.testsys.webclient.util.exception.TrikException

/**
 * @since 1.1.0.14-alpha
 */
class TrikIllegalArgumentException(message: String) : IllegalArgumentException(message), TrikException