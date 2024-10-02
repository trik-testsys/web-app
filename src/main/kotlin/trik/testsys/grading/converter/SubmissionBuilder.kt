package trik.testsys.grading.converter

import com.google.protobuf.ByteString
import trik.testsys.grading.*
import trik.testsys.grading.GradingNodeOuterClass.Submission
import trik.testsys.webclient.entity.Solution
import trik.testsys.webclient.entity.Task
import trik.testsys.webclient.service.Grader
import java.io.File

class SubmissionBuilder private constructor() {
    lateinit var solution: Solution
    lateinit var solutionFile: File

    lateinit var task: Task
    lateinit var taskFiles: Collection<File>

    lateinit var gradingOptions: Grader.GradingOptions

    companion object {
        fun build(block: SubmissionBuilder.() -> Unit): Submission {
            val sb = SubmissionBuilder()
            block(sb)

            val submission = submission {
                id = sb.solution.id.toInt()

                this.task = task {
                    fields.addAll(
                        sb.taskFiles.map {
                            file {
                                name = it.name
                                content = ByteString.readFrom(it.inputStream())
                            }
                        }
                    )
                }

                options = options {
                    dockerImage = sb.gradingOptions.trikStudioVersion
                    recordVideo = sb.gradingOptions.shouldRecordRun
                }

                visualLanguageSubmission = visualLanguageSubmission {
                    file = file {
                        name = sb.solutionFile.name
                        content = ByteString.readFrom(sb.solutionFile.inputStream())
                    }
                }
            }
            return submission
        }
    }
}
