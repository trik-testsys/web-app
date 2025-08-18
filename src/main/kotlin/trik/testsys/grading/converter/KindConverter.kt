package trik.testsys.grading.converter

import trik.testsys.webclient.service.Grader.ErrorKind

object KindConverter {
    fun convert(kind: Int, description: String): ErrorKind {
        return when (kind) {
            1 -> ErrorKind.UnexpectedException(description)
            2 -> {
                val code = parseExitCode(description)
                ErrorKind.NonZeroExitCode(code, description)
            }
            3 -> ErrorKind.MismatchedFiles(description)
            4 -> ErrorKind.InnerTimeoutExceed(description)
            5 -> ErrorKind.UnsupportedImageVersion(description)
            else -> ErrorKind.Unknown(description)
        }
    }

    private fun parseExitCode(str: String) =
        str.split(' ')
            .last()
            .toInt()
}