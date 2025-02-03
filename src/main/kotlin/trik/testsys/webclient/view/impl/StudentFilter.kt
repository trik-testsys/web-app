package trik.testsys.webclient.view.impl

data class StudentFilter(
    val studentId: Long?,
    val groupId: Long?,
    val adminId: Long?,
    val contestId: Long?,
    val taskId: Long?,
    val solutionId: Long?
) {

    fun isEmpty() = studentId == null && groupId == null && adminId == null && contestId == null && taskId == null && solutionId == null

    companion object {
        fun empty() = StudentFilter(null, null, null, null, null, null)
    }
}