package trik.testsys.grading.converter

import trik.testsys.grading.GradingNodeOuterClass.File
import trik.testsys.webclient.service.Grader

class FileConverter {
    fun convert(file: File): Grader.GradingInfo.File {
        return Grader.GradingInfo.File(file.name, file.content.toByteArray())
    }
}
