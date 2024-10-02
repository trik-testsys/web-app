package trik.testsys.grading.converter

import trik.testsys.grading.GradingNodeOuterClass.FieldResult
import trik.testsys.grading.videoOrNull
import trik.testsys.webclient.service.Grader

class FieldResultConverter(private val fileConverter: FileConverter) {
    fun convert(fieldResult: FieldResult): Grader.GradingInfo.FieldResult {
        val name = fieldResult.name
        val verdict = fieldResult.verdict.let { fileConverter.convert(it) }
        val recording = fieldResult.videoOrNull?.let { fileConverter.convert(it) }
        return Grader.GradingInfo.FieldResult(name, verdict, recording)
    }
}
