package trik.testsys.webapp.grading.converter

import trik.testsys.webapp.backoffice.service.Grader

object KindConverter {
    fun convert(kind: Int, description: String): Grader.ErrorKind {
        return when (kind) {
            1 -> Grader.ErrorKind.UnexpectedException(description)
            2 -> {
                val code = parseExitCode(description)
                Grader.ErrorKind.NonZeroExitCode(code, description)
            }
            3 -> Grader.ErrorKind.MismatchedFiles(description)
            4 -> Grader.ErrorKind.InnerTimeoutExceed(description)
            5 -> Grader.ErrorKind.UnsupportedImageVersion(description)
            else -> Grader.ErrorKind.Unknown(description)
        }
    }

    private fun parseExitCode(str: String) = try {
            str.split(' ')
                .last()
                .toInt()
        } catch (e: Exception) {
            500
        }
}