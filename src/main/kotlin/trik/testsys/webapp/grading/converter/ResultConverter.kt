package trik.testsys.webapp.grading.converter

import trik.testsys.grading.GradingNodeOuterClass
import trik.testsys.grading.okOrNull
import trik.testsys.webapp.backoffice.service.Grader

/**
 * @author Vyacheslav Buchin
 * @since 3.12.0
 */
class ResultConverter(private val fieldResultConverter: FieldResultConverter) {
    fun convert(result: GradingNodeOuterClass.Result): Grader.GradingInfo {
        val submissionId = result.id

        return result.okOrNull?.let { ok ->
            val fieldResults = ok.resultsList.map { fieldResultConverter.convert(it) }
            Grader.GradingInfo.Ok(submissionId, fieldResults)
        } ?: result.error.let {
            val errorKind = KindConverter.convert(it.kind, it.description)
            Grader.GradingInfo.Error(submissionId, errorKind)
        }
    }

}