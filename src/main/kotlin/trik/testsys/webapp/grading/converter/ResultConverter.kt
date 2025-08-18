//package trik.testsys.webapp.grading.converter
//
//import trik.testsys.grading.GradingNodeOuterClass
//import trik.testsys.grading.okOrNull
//import trik.testsys.backoffice.service.Grader
//
//class ResultConverter(private val fieldResultConverter: FieldResultConverter) {
//    fun convert(result: GradingNodeOuterClass.Result): Grader.GradingInfo {
//        val submissionId = result.id
//
//        return result.okOrNull?.let { ok ->
//            val fieldResults = ok.resultsList.map { fieldResultConverter.convert(it) }
//            Grader.GradingInfo.Ok(submissionId, fieldResults)
//        } ?: result.error.let {
//            Grader.GradingInfo.Error(submissionId, it.kind, it.description)
//        }
//    }
//
//}
