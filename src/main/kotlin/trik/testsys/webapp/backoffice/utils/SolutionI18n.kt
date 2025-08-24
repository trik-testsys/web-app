package trik.testsys.webapp.backoffice.utils

import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.Solution

@Service("solutionI18n")
class SolutionI18n {

    private val statusToRu: Map<Solution.Status, String> = mapOf(
        Solution.Status.NOT_STARTED to "В очереди",
        Solution.Status.IN_PROGRESS to "В процессе",
        Solution.Status.PASSED to "Пройдено",
        Solution.Status.ERROR to "Ошибка",
    )

    fun toRu(status: Solution.Status): String = statusToRu[status] ?: status.name
}


