package trik.testsys.webapp.grading.converter

import trik.testsys.grading.GradingNodeOuterClass.File
import trik.testsys.webapp.backoffice.service.Grader

/**
 * @author Vyacheslav Buchin
 * @since 3.12.0
 */
class FileConverter {
    fun convert(file: File): Grader.GradingInfo.File {
        return Grader.GradingInfo.File(file.name, file.content.toByteArray())
    }
}
