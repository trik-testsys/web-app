package trik.testsys.webclient.view.impl

data class StudentSolutionFilter(
    val taskId: Long?,
    val solutionId: Long?
) {

    fun isEmpty() = taskId == null && solutionId == null

    companion object {
        fun empty() = StudentSolutionFilter(null, null)
    }
}