//package trik.testsys.webapp.grading.converter
//
//import com.google.protobuf.ByteString
//import trik.testsys.grading.*
//import trik.testsys.grading.GradingNodeOuterClass.Submission
//import trik.testsys.backoffice.entity.impl.Solution
//import trik.testsys.backoffice.entity.impl.Task
//import trik.testsys.backoffice.service.Grader
//import java.io.File
//
//class SubmissionBuilder private constructor() {
//    lateinit var solution: Solution
//    lateinit var solutionFile: File
//
//    lateinit var task: Task
//    lateinit var taskFiles: Collection<File>
//
//    lateinit var gradingOptions: Grader.GradingOptions
//
//    companion object {
//        fun build(block: SubmissionBuilder.() -> Unit): Submission {
//            val sb = SubmissionBuilder()
//            block(sb)
//
//            val submission = submission {
//                id = sb.solution.id?.let {
//                    it.toInt()
//                } ?: throw NullPointerException("Solution id cannot be null.")
//
//                task = task {
//                    fields.addAll(
//                        sb.taskFiles.map {
//                            file {
//                                name = it.name
//                                content = ByteString.readFrom(it.inputStream())
//                            }
//                        }
//                    )
//                }
//
//                options = options {
//                    dockerImage = sb.gradingOptions.trikStudioVersion
//                    recordVideo = sb.gradingOptions.shouldRecordRun
//                }
//
//                fillSubmission(sb.solution, sb.solutionFile)
//            }
//            return submission
//        }
//    }
//}
//
//private fun SubmissionKt.Dsl.fillSubmission(solution: Solution, solutionFile: File) {
//    val encodedFile = file {
//        name = solutionFile.name
//        content = ByteString.readFrom(solutionFile.inputStream())
//    }
//    when (solution.type) {
//        Solution.SolutionType.QRS -> visualLanguageSubmission = visualLanguageSubmission { file = encodedFile }
//        Solution.SolutionType.PYTHON -> pythonSubmission = pythonSubmission { file = encodedFile }
//        Solution.SolutionType.JAVASCRIPT -> javascriptSubmission = javaScriptSubmission { file = encodedFile }
//    }
//}
