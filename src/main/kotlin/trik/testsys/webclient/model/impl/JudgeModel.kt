package trik.testsys.webclient.model.impl

import trik.testsys.webclient.controller.impl.JudgeController
import trik.testsys.webclient.entity.impl.SolutionAction
import trik.testsys.webclient.model.TrikModel
import trik.testsys.webclient.util.exception.impl.TrikIllegalStateException
import java.time.LocalDateTime

class JudgeModel private constructor(
    val accessToken: String,
    val username: String,
    val additionalInfo: String,
    val registrationDate: LocalDateTime?,
    val lastLoginDate: LocalDateTime?,
    val filterParams: Map<String, Any?>,
    val solutions: Collection<JudgeController.SolutionRow>?,
    val actions: Collection<SolutionAction>
) : TrikModel {

    class Builder internal constructor() {

        private var accessToken: String? = null
        private var username: String? = null
        private var additionalInfo: String? = null
        private var registrationDate: LocalDateTime? = null
        private var lastLoginDate: LocalDateTime? = null
        private var filterParams: Map<String, Any?>? = null
        private var solutions: Collection<JudgeController.SolutionRow>? = null
        private var actions: Collection<SolutionAction>? = null

        fun accessToken(accessToken: String) = apply { this.accessToken = accessToken }

        fun username(username: String) = apply { this.username = username }

        fun additionalInfo(additionalInfo: String?) = apply { this.additionalInfo = additionalInfo }

        fun registrationDate(registrationDate: LocalDateTime?) = apply { this.registrationDate = registrationDate }

        fun lastLoginDate(lastLoginDate: LocalDateTime?) = apply { this.lastLoginDate = lastLoginDate }

        fun filterParams(filterParams: Map<String, Any?>?) = apply { this.filterParams = filterParams }

        fun solutions(solutions: Collection<JudgeController.SolutionRow>?) = apply { this.solutions = solutions }

        fun actions(actions: Collection<SolutionAction>?) = apply { this.actions = actions }

        fun build() = JudgeModel(
            accessToken ?: throw TrikIllegalStateException(String.format(PARAMETER_ERROR, JudgeModel::accessToken.name)),
            username ?: throw TrikIllegalStateException(String.format(PARAMETER_ERROR, JudgeModel::username.name)),
            additionalInfo ?: throw TrikIllegalStateException(String.format(PARAMETER_ERROR, JudgeModel::additionalInfo.name)),
            registrationDate,
            lastLoginDate,
            filterParams ?: emptyMap(),
            solutions,
            actions ?: emptyList()
        )
    }

    override fun asMap(): Map<String, Any?> {
        val argsMap = mutableMapOf<String, Any?>()

        argsMap[this::accessToken.name] = accessToken
        argsMap[this::username.name] = username
        argsMap[this::additionalInfo.name] = additionalInfo
        argsMap[this::registrationDate.name] = registrationDate
        argsMap[this::lastLoginDate.name] = lastLoginDate
        argsMap[this::filterParams.name] = filterParams
        argsMap[this::solutions.name] = solutions
        argsMap[this::actions.name] = actions.sortedByDescending { it.dateTime }

        return argsMap
    }

    companion object {
        private const val PARAMETER_ERROR = "Parameter %s is null"
    }
}